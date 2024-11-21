import pandas as pd
from lxml import etree
from pathlib import Path
from rapidfuzz import fuzz
from rapidfuzz.distance import Levenshtein
from multiprocessing import Pool
from functools import partial
import time
from datetime import timedelta
from pybloom_live import BloomFilter
import math
import sys
import logging
from datetime import datetime
from pathlib import Path

# Define custom output path
CUSTOM_OUTPUT_PATH = Path('/Users/anhnhat/Library/Mobile Documents/com~apple~CloudDocs/Documents/UNIMA/2. Semester Study/2. WS2425/4. W24 Web Data Integration/Project/UMA_Web_Data_Integation/LLM_Gold_Standard')
data_dir = Path('/Users/anhnhat/Library/Mobile Documents/com~apple~CloudDocs/Documents/UNIMA/2. Semester Study/2. WS2425/4. W24 Web Data Integration/Project/UMA_Web_Data_Integation/data')

# Ensure data directory exists
#data_dir.mkdir(parents=True, exist_ok=True)

def setup_logging(output_path):
    """Setup logging with custom output path"""
    # Create logs directory
    log_dir = output_path / 'logs'
    log_dir.mkdir(parents=True, exist_ok=True)
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    log_file = log_dir / f'similarity_comparison_{timestamp}.log'
    
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler(log_file),
            logging.StreamHandler(sys.stdout)
        ]
    )
    logging.info(f"Log file created at: {log_file}")
    return log_file

def parse_xml_to_df(xml_path):
    try:
        # Verify file exists
        if not xml_path.exists():
            raise FileNotFoundError(f"XML file not found: {xml_path}")
        
        # Parse XML
        parser = etree.XMLParser(recover=True)
        tree = etree.parse(str(xml_path), parser)
        root = tree.getroot()
        
        # Extract song data
        data = []
        for song in root.findall('.//song'):
            item = {
                'id': song.find('id').text if song.find('id') is not None else None,
                'Artist': song.find('Artist').text if song.find('Artist') is not None else None,
                'Track': song.find('Track').text if song.find('Track') is not None else None,
                'Album': song.find('Album').text if song.find('Album') is not None else None,
                'Track_Explicitness': song.find('Track_Explicitness').text if song.find('Track_Explicitness') is not None else None,
                'Album_Year': song.find('Album_Year').text if song.find('Album_Year') is not None else None,
                'Duration': song.find('Duration').text if song.find('Duration') is not None else None
            }
            data.append(item)
            
        return pd.DataFrame(data)
        
    except Exception as e:
        logging.error(f"Error parsing {xml_path}: {str(e)}")
        raise

def concat_artist_track(artist, track, album_year):
    """Combine artist, track, and album year into a single string"""
    return f"{str(artist)} {str(track)} {str(album_year)}".lower().strip()

def classify_similarity(similarity):
    """
    Classify normalized similarity scores (0-1 range):
    1: High similarity (>=0.75)
    0: Medium similarity (0.65-0.75)
    -1: Low similarity (<0.65)
    """
    if similarity >= 0.75:
        return 1
    elif 0.65 <= similarity < 0.75:
        return 0
    else:
        return -1

def get_first_two_letters(text):
    """Get first two letters of text, handling edge cases"""
    return str(text)[:2].lower() if pd.notna(text) else ''

def create_block_bloom_filter(block_df, estimated_items):
    """Create a Bloom filter for a block of records"""
    # False positive probability of 0.1%
    bloom = BloomFilter(capacity=estimated_items, error_rate=0.001)
    for item in block_df['opendb_combined']:
        bloom.add(item.lower())
    return bloom

def process_block_with_bloom(source_block, target_block):
    """Process a block using Bloom filter pre-filtering"""
    results = []
    
    bloom = create_block_bloom_filter(
        target_block, 
        estimated_items=len(target_block)
    )
    
    for _, src_row in source_block.iterrows():
        if src_row['source_combined'].lower() in bloom:
            for _, tgt_row in target_block.iterrows():
                similarity = Levenshtein.normalized_similarity(
                    src_row['source_combined'],
                    tgt_row['opendb_combined']
                    #score_cutoff=0.65
                )
                if similarity > 0:
                    results.append({
                        'source_id': src_row['source_id'],
                        'opendb_id': tgt_row['opendb_id'],
                        'source_artist': src_row['apple_Artist' if 'apple_Artist' in src_row else 'million_Artist'],
                        'source_track': src_row['apple_Track' if 'apple_Track' in src_row else 'million_Track'],
                        'opendb_artist': tgt_row['opendb_Artist'],
                        'opendb_track': tgt_row['opendb_Track'],
                        'source_combined': src_row['source_combined'],
                        'opendb_combined': tgt_row['opendb_combined'],
                        'similarity_score': round(similarity, 3),
                        'match_classification': classify_similarity(similarity)
                    })
    return results

# Update compare_datasets function to use Bloom filtering
def compare_datasets(source_df, target_df, source_id_col):
    """Compare datasets using blocking and Bloom filtering"""
    logging.info(f"\nComparing {len(source_df)} source records with {len(target_df)} target records")
    
    source_df['block_key'] = source_df['source_combined'].apply(get_first_two_letters)
    target_df['block_key'] = target_df['opendb_combined'].apply(get_first_two_letters)
    
    results = []
    unique_prefixes = source_df['block_key'].unique()
    total_blocks = len(unique_prefixes)
    total_comparisons = 0
    bloom_filtered = 0
    
    logging.info(f"\nTotal number of blocks: {total_blocks}")
    
    for idx, prefix in enumerate(unique_prefixes, 1):
        source_block = source_df[source_df['block_key'] == prefix]
        target_block = target_df[target_df['block_key'] == prefix]
        
        if len(source_block) > 0 and len(target_block) > 0:
            block_comparisons = len(source_block) * len(target_block)
            total_comparisons += block_comparisons
            
            if idx % 10 == 0 or idx == 1:
                logging.info(f"\nProcessing block '{prefix}':")
                logging.info(f"- Records before Bloom filter: {block_comparisons:,}")
            
            block_results = process_block_with_bloom(source_block, target_block)
            results.extend(block_results)
            bloom_filtered += len(block_results)
            
            if idx % 10 == 0:
                logging.info(f"- Records after Bloom filter: {len(block_results):,}")
    
    logging.info(f"\nFinal Statistics:")
    logging.info(f"Total comparisons before Bloom filter: {total_comparisons:,}")
    logging.info(f"Total comparisons after Bloom filter: {bloom_filtered:,}")
    logging.info(f"Reduction: {((total_comparisons-bloom_filtered)/total_comparisons)*100:.2f}%")
    
    return pd.DataFrame(results)

try:
    start_time = time.time()
    
    # Setup logging with custom path
    log_file = setup_logging(CUSTOM_OUTPUT_PATH)
    logging.info("Starting similarity comparison process")
    
    # Read XML files with logging
    logging.info("\n=== Loading XML files ===")
    load_start = time.time()
    xml_dir = data_dir / 'input'
    xml_dir.mkdir(parents=True, exist_ok=True)
    df_apple_xml = parse_xml_to_df(xml_dir / 'apple.xml')
    df_million_xml = parse_xml_to_df(xml_dir / 'million.xml')
    df_opendb_xml = parse_xml_to_df(xml_dir / 'opendb.xml')
    load_time = time.time() - load_start
    logging.info(f"Loading time: {timedelta(seconds=load_time)}")
    logging.info(f"Loaded Apple XML with {len(df_apple_xml)} records")
    logging.info(f"Loaded Million XML with {len(df_million_xml)} records")
    logging.info(f"Loaded OpenDB XML with {len(df_opendb_xml)} records")
    
    # Add prefixes with logging
    logging.info("\n=== Adding prefixes and combining columns ===")
    prep_start = time.time()
    if not df_apple_xml.empty:
        df_apple_xml = df_apple_xml.add_prefix('apple_')
    if not df_million_xml.empty:
        df_million_xml = df_million_xml.add_prefix('million_')
    if not df_opendb_xml.empty:
        df_opendb_xml = df_opendb_xml.add_prefix('opendb_')
        
    # Create combined columns
    df_apple_xml['apple_combined'] = df_apple_xml.apply(
        lambda x: concat_artist_track(x['apple_Artist'], x['apple_Track'], x['apple_Album_Year']), axis=1
    )
    df_opendb_xml['opendb_combined'] = df_opendb_xml.apply(
        lambda x: concat_artist_track(x['opendb_Artist'], x['opendb_Track'], x['opendb_Album_Year']), axis=1
    )
    df_million_xml['million_combined'] = df_million_xml.apply(
        lambda x: concat_artist_track(x['million_Artist'], x['million_Track'], x['million_Album_Year']), axis=1
    )
    prep_time = time.time() - prep_start
    logging.info(f"Preprocessing time: {timedelta(seconds=prep_time)}")
    
    # Prepare datasets
    df_apple_compare = df_apple_xml.rename(columns={
        'apple_id': 'source_id',
        'apple_combined': 'source_combined'
    })
    df_million_compare = df_million_xml.rename(columns={
        'million_id': 'source_id',
        'million_combined': 'source_combined'
    })
    
    logging.info("\n=== Calculating Apple-OpenDB similarities ===")
    apple_comp_start = time.time()
    df_similarity_apple = compare_datasets(df_apple_compare, df_opendb_xml, 'source_id')
    apple_comp_time = time.time() - apple_comp_start
    logging.info(f"Apple comparison time: {timedelta(seconds=apple_comp_time)}")
    
    logging.info("\n=== Calculating Million-OpenDB similarities ===")
    million_comp_start = time.time()
    df_similarity_million = compare_datasets(df_million_compare, df_opendb_xml, 'source_id')
    million_comp_time = time.time() - million_comp_start
    logging.info(f"Million comparison time: {timedelta(seconds=million_comp_time)}")
    
    # Save results to custom output directory
    output_dir = CUSTOM_OUTPUT_PATH / 'output'
    output_dir.mkdir(parents=True, exist_ok=True)
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    
    # Save Apple similarities
    apple_output = output_dir / f'apple_opendb_similarities_{timestamp}.csv'
    df_similarity_apple.to_csv(apple_output, index=False, float_format='%.3f')
    logging.info(f"\nSaved Apple-OpenDB similarities to: {apple_output}")
    logging.info(f"Records saved: {len(df_similarity_apple)}")
    
    # Save Million similarities
    million_output = output_dir / f'million_opendb_similarities_{timestamp}.csv'
    df_similarity_million.to_csv(million_output, index=False, float_format='%.3f')
    logging.info(f"\nSaved Million-OpenDB similarities to: {million_output}")
    logging.info(f"Records saved: {len(df_similarity_million)}")
    
    total_time = time.time() - start_time
    logging.info(f"\nTotal execution time: {timedelta(seconds=total_time)}")

    logging.info("\n=== Classification Results ===")
    logging.info("\nApple-OpenDB Classifications:")
    logging.info(df_similarity_apple['match_classification'].value_counts().sort_index())
    logging.info("\nMillion-OpenDB Classifications:")
    logging.info(df_similarity_million['match_classification'].value_counts().sort_index())

except Exception as e:
    logging.error(f"Error saving results: {e}", exc_info=True)
    raise