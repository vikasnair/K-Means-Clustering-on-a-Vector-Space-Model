# K-Means Clustering performed on a Vector Space Model
## Authors: Vikas Nair, Christopher Okorodudu, Dasha Tsenter

This program implements a version of k-means clustering on unstructured text data. 

A note on the data: three directories contain eight articles, each respecting similar topics per folder.

First, the raw text of each article is first pre-processed: the algorithm strips redundant substrings (stop words) and stitches together NER tags (Microsoft Corporation -> Microsoft_Corporation). Next, the data is formatted into a vector space model. Each document folder is represented as a 2D matrix of the non-redundant keywords. A text file can be generated of the common keywords (or topics) per directory. Finally, the document vectors are merged into one large vector and k-means clustering is deployed to recluster the documents into three respective clusters using both euclidian and cosine similarity algorithms. A visualization of the clustering launches at completion.

Please allow some time for the algorithm to complete and visualization to appear.

### Required dependencies:

Stanford Core NLP (Simple)

The particular .jar libraries used are enumerated below.

### Compile
```
javac -cp stanford-corenlp-3.9.1.jar:slf4j-simple.jar:protobuf.jar:stanford-corenlp-3.9.1-models.jar Main.java
```

### Run
```
java -cp .:stanford-corenlp-3.9.1.jar:slf4j-simple.jar:protobuf.jar:stanford-corenlp-3.9.1-models.jar Main
```
