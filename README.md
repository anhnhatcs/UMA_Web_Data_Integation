# Web Data Integration Project

## Overview

This project is a comprehensive implementation of **data integration** for song metadata, focusing on integrating datasets from multiple sources. The project employs techniques such as blocking, similarity computation, fusion strategies, and evaluation to handle heterogeneous schemas and achieve a unified view of the data.

---

## Project Structure

### **1. Data**
This folder contains the raw input data, gold standard references, and output generated during processing. The subfolders include:
- **Input**: Original XML files representing the data sources:
    - `apple.xml`
    - `million.xml`
    - `opendb.xml`
- **Correspondences**: CSV files documenting correspondences between data sources.
    - Example: `apple_opendb_correspondences.csv`
- **Goldstandard**: Reference datasets for evaluation purposes.
    - Example: `gold.xml`, `gs_million_opendb.csv`
- **Output**: Results from blocking and matching rules, including debug outputs for analysis.
    - Example: `debugResultsBlocking.csv`

---

### **2. Blocking**
Contains classes for generating blocking keys, which group potential matches efficiently. Key files include:
- `SongBlockingKeyByAlbumGenerator`
- `SongBlockingKeyByArtistGenerator`
- `SongBlockingKeyByTitleGenerator`

---

### **3. Comparators**
Defines similarity measures for comparing attributes such as titles, artists, and albums. Examples:
- **String Comparators**:
    - `Jaro`, `JaroWinkler`, `Levenshtein`, `Soundex`
- **Numeric Comparators**:
    - `SongDurationComparator10Seconds`
- **Custom Comparators**:
    - `SongTitleComparatorLowerCaseJaccard`

---

### **4. Evaluation**
Includes rules to assess the accuracy of matching and fusion by comparing results against the gold standard. Examples:
- `AlbumEvaluationRule`
- `ArtistEvaluationRule`

---

### **5. Fusers**
Handles merging data from different sources based on specific strategies, such as:
- Favoring a source: `AlbumFuserFavourSource`
- String length-based: `ArtistFuserShortestString`, `AlbumFuserLongestString`
- Voting strategies: `TitleFuserVoting`

---

### **6. Model**
Utility classes and main functions for integration:
- **Core Classes**:
    - `Song`: Represents song data.
    - `SongXMLReader`, `SongXMLFormatter`, `SongCSVFormatter`: For data reading and writing.
- **Main File**:
    - `DataFusion_Main`: Executes the data fusion workflows.

---

### **7. Reports**
Contains documentation and presentation materials summarizing the project:
- **Final Report**:
    - `WDI_Group1_Final.pdf`: Details the methods, implementation, and results of the project.

---

### **8. LLM_Gold_Standard**
Scripts and datasets for generating and validating gold standards using large language models.

---

### **9. RapidFuzz_Gold_Standard**
Scripts and notebooks for creating gold standards using RapidFuzz similarity metrics.

---

## Usage

1. **Prepare Input Data**: Place input XML files in the `data/input` folder.
2. **Run Blocking**: Execute the blocking module to generate groups of potential matches.
3. **Run Comparators**: Apply similarity measures to compare attributes within blocks.
4. **Perform Data Fusion**: Use the fusers to integrate data and resolve conflicts based on defined rules.
5. **Evaluate Results**: Compare outputs with the gold standard for performance assessment.

---

## Requirements

- Java Development Kit (JDK)
- Maven for dependency management
- Python (for auxiliary scripts in `LLM_Gold_Standard` and `RapidFuzz_Gold_Standard`)

---

## Contributors

This project was developed as part of a Web Data Integration Project at the University of Mannheim.

The contributors are:
- Anh-Nhat Nguyen
- Ching-Yun Cheng
- Shamalan Rajesvaran
- Yen-An Chen
- Phelan Lee Yeuk Bun
