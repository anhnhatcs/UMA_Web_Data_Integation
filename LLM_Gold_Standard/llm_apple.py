import pandas as pd
import openai
import time
import os
from dotenv import load_dotenv
from typing import Dict
# from openai import OpenAI  # New import style

# Load environment variables
load_dotenv()
openai.api_key = os.getenv("OPENAI_API_KEY")

# Initialize the client
# client = OpenAI()  # Make sure your OPENAI_API_KEY is set in environment variables

# Constants for similarity matching
DURATION_THRESHOLD_MS = 3000
SIMILARITY_THRESHOLD = 0.85

class MusicMatcher:
    def __init__(self, csv_path: str):
        self.csv_path = csv_path
        self.df = pd.read_csv(csv_path)
        self.results = []
        
    def compare_tracks(self, row: Dict) -> Dict:
        try:
            prompt = f"""You are a music data expert. Compare these music tracks represent the same real-world song.
            
            Data to compare:
            apple_Artist: {row['apple_Artist']} vs opendb_Artist: {row['opendb_Artist']}
            apple_Track: {row['apple_Track']} vs opendb_Track: {row['opendb_Track']}
            apple_Album: {row['apple_Album']} vs opendb_Album: {row['opendb_Album']}
            apple_Year: {row['apple_Album_Year']} vs opendb_Year: {row['opendb_Album_Year']}
            apple_Duration: {row['apple_Duration']} vs opendb_Duration: {row['opendb_Duration']}

            Rules:
            1. Names should be compared case-insensitively, allowing for semantic similarity rather than exact matches
            2. Duration difference should be within ±3000ms
            3. Year difference should be within ±1 year
            4. All attributes must match within thresholds

            Return only: 1 if same song, 0 if different"""
            
            response = openai.ChatCompletion.create(
                model="gpt-4o-mini",
                messages=[
                    {"role": "system", "content": "You are a music matching expert. Respond only with 1 or 0."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.1
            )
            
            return {"match": response.choices[0].message.content.strip()}
            
        except Exception as e:
            print(f"Error in API call: {str(e)}")
            return {"match": "0"}
            
    def save_results(self, output_path: str):
        results_df = pd.DataFrame(self.results)
        results_df.to_csv(output_path, index=False)

if __name__ == "__main__":
    # Test file path
    file_path = "/Users/anhnhat/Library/Mobile Documents/com~apple~CloudDocs/Documents/UNIMA/2. Semester Study/2. WS2425/4. W24 Web Data Integration/Project/UMA_Web_Data_Integation/LLM_Gold_Standard/apple_merged.csv"
    matcher = MusicMatcher(file_path)
    
    total_rows = len(matcher.df)
    print(f"\nProcessing {total_rows} rows...")
    print("-" * 50)

    for index, row in matcher.df.iterrows():
        try:
            result = matcher.compare_tracks(row)
            matcher.results.append({
                **row,  # Unpack all columns from the row
                "match_gpt4o": result["match"]  # Add match result with new column name
            })
            
            # Print progress every 10 rows
            if (index + 1) % 10 == 0:
                print(f"Processed {index + 1}/{total_rows} rows")
                
        except Exception as e:
            print(f"Error processing row {index}: {str(e)}")
            continue

    print("\nProcessing complete!")
    print("-" * 50)
    
    # Save all results
    matcher.save_results("complete_results.csv")
    print(f"Results saved to complete_results.csv")