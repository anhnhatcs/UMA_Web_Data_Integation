import pandas as pd
from lxml import etree
from pathlib import Path

# Get current directory and setup paths
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent
data_dir = project_root / 'data'

# Read CSV files
df_apple = pd.read_csv(data_dir / 'goldstandard/gs_opendb_apple.csv', header=None)
df_million = pd.read_csv(data_dir / 'goldstandard/gs_million_opendb.csv', header=None)

# Rename CSV columns
df_apple.columns = ['o_apple_key', 'o_opendb_key', 'o_flag']
df_million.columns = ['m_million_key', 'm_opendb_key', 'm_flag']

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
        print(f"Error parsing {xml_path}: {str(e)}")
        raise

try:
    # Read XML files
    xml_dir = data_dir / 'input'
    df_apple_xml = parse_xml_to_df(xml_dir / 'apple.xml')
    df_million_xml = parse_xml_to_df(xml_dir / 'million.xml')
    df_opendb_xml = parse_xml_to_df(xml_dir / 'opendb.xml')
    
    # Add prefixes
    if not df_apple_xml.empty:
        df_apple_xml = df_apple_xml.add_prefix('apple_')
    if not df_million_xml.empty:
        df_million_xml = df_million_xml.add_prefix('million_')
    if not df_opendb_xml.empty:
        df_opendb_xml = df_opendb_xml.add_prefix('opendb_')
    
    print("\nDataset shapes:")
    print(f"Apple XML: {df_apple_xml.shape}")
    print(f"Million XML: {df_million_xml.shape}")
    print(f"OpenDB XML: {df_opendb_xml.shape}")
    
    # Convert IDs to string for consistent matching
    df_apple['o_apple_key'] = df_apple['o_apple_key'].astype(str)
    df_apple['o_opendb_key'] = df_apple['o_opendb_key'].astype(str)
    df_million['m_million_key'] = df_million['m_million_key'].astype(str)
    df_million['m_opendb_key'] = df_million['m_opendb_key'].astype(str)
    
    # Convert XML IDs to string
    if not df_apple_xml.empty:
        df_apple_xml['apple_id'] = df_apple_xml['apple_id'].astype(str)
    if not df_million_xml.empty:
        df_million_xml['million_id'] = df_million_xml['million_id'].astype(str)
    if not df_opendb_xml.empty:
        df_opendb_xml['opendb_id'] = df_opendb_xml['opendb_id'].astype(str)

    # Merge Apple datasets (CSV + XML + OpenDB)
    df_apple_merged = pd.merge(
        df_apple,
        df_apple_xml,
        left_on='o_apple_key',
        right_on='apple_id',
        how='left'
    )
    df_apple_merged = pd.merge(
        df_apple_merged,
        df_opendb_xml,
        left_on='o_opendb_key',
        right_on='opendb_id',
        how='left'
    )

    # Merge Million datasets (CSV + XML + OpenDB)
    df_million_merged = pd.merge(
        df_million,
        df_million_xml,
        left_on='m_million_key',
        right_on='million_id',
        how='left'
    )
    df_million_merged = pd.merge(
        df_million_merged,
        df_opendb_xml,
        left_on='m_opendb_key',
        right_on='opendb_id',
        how='left'
    )

    print("\nMerged dataset shapes:")
    print(f"Apple merged: {df_apple_merged.shape}")
    print(f"Million merged: {df_million_merged.shape}")
    print("\nApple merged columns:", df_apple_merged.columns.tolist())
    print("\nMillion merged columns:", df_million_merged.columns.tolist())

    # Create output directory

    # Save Apple merged dataset
    apple_output =  '/Users/anhnhat/Library/Mobile Documents/com~apple~CloudDocs/Documents/UNIMA/2. Semester Study/2. WS2425/4. W24 Web Data Integration/Project/UMA_Web_Data_Integation/LLM_Gold_Standard/apple_merged.csv'
    df_apple_merged.to_csv(apple_output, index=False)
    
    # Save Million merged dataset
    # million_output = '/Users/anhnhat/Library/Mobile Documents/com~apple~CloudDocs/Documents/UNIMA/2. Semester Study/2. WS2425/4. W24 Web Data Integration/Project/UMA_Web_Data_Integation/LLM_Gold_Standard/million_merged.csv'
    # df_million_merged.to_csv(million_output, index=False)
    
    print("\nFiles saved successfully:")
    print(f"Apple merged data: {apple_output}")
    #print(f"Million merged data: {million_output}")
    print(f"Apple rows: {len(df_apple_merged)}")
    #print(f"Million rows: {len(df_million_merged)}")
    print(df_apple.head())
except Exception as e:
    print(f"Error saving files: {e}")
    raise

