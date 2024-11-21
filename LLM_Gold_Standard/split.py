import pandas as pd
import numpy as np
from pathlib import Path

# Define base path
base_path = Path('/Users/anhnhat/Library/Mobile Documents/com~apple~CloudDocs/Documents/UNIMA/2. Semester Study/2. WS2425/4. W24 Web Data Integration/Project/UMA_Web_Data_Integation/LLM_Gold_Standard/output')

def sample_and_save(input_file, output_file):
    print(f"\nProcessing: {input_file.name}")
    
    # Read CSV
    df = pd.read_csv(input_file)
    
    # Get available counts
    neg_available = len(df[df['match_classification'] == -1])
    pos_available = len(df[df['match_classification'] == 1])
    neut_available = len(df[df['match_classification'] == 0])
    
    # Calculate desired samples
    total_desired = 2000
    neg_desired = int(total_desired * 0.5)  # 50% of -1
    pos_desired = int(total_desired * 0.2)  # 20% of 1
    neut_desired = int(total_desired * 0.3)  # 30% of 0
    
    # Adjust sample sizes if necessary
    neg_samples = min(neg_desired, neg_available)
    pos_samples = min(pos_desired, pos_available)
    neut_samples = min(neut_desired, neut_available)
    
    print("\nSampling Statistics:")
    print(f"Negative (-1): {neg_samples}/{neg_available} (desired: {neg_desired})")
    print(f"Positive (1): {pos_samples}/{pos_available} (desired: {pos_desired})")
    print(f"Neutral (0): {neut_samples}/{neut_available} (desired: {neut_desired})")
    
    # Sample from each group
    neg_df = df[df['match_classification'] == -1].sample(n=neg_samples, random_state=42)
    pos_df = df[df['match_classification'] == 1].sample(n=pos_samples, random_state=42)
    neut_df = df[df['match_classification'] == 0].sample(n=neut_samples, random_state=42)
    
    # Combine and shuffle
    sampled_df = pd.concat([neg_df, pos_df, neut_df])
    sampled_df = sampled_df.sample(frac=1, random_state=42).reset_index(drop=True)
    
    # Save
    sampled_df.to_csv(output_file, index=False)
    print(f"\nTotal samples saved: {len(sampled_df)}")
    print(f"Saved to: {output_file}")
    print("\nFinal distribution:")
    print(sampled_df['match_classification'].value_counts())

# Process both files
million_input = base_path / 'million_opendb_similarities_20241117_221340.csv'
million_output = base_path / 'million_opendb_sampled_1000_v2.csv'
sample_and_save(million_input, million_output)

apple_input = base_path / 'apple_opendb_similarities_20241117_221340.csv'
apple_output = base_path / 'apple_opendb_sampled_1000_v2.csv'
sample_and_save(apple_input, apple_output)