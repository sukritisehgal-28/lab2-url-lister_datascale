# Lab 2 — UrlCount (Hadoop MapReduce)

## What this does
Counts URLs in Wikipedia HTML (matches `href="..."` or `href='...'`) and prints only those with **count > 5**.

## How to run
```bash
# compile
javac -classpath "$(hadoop classpath)" -d . UrlCount.java
jar cf UrlCount.jar UrlCount*.class
rm -f UrlCount*.class

# input in HDFS (files already downloaded to home as Apache_Hadoop.html / MapReduce.html)
hadoop fs -mkdir -p input
hadoop fs -put -f ~/Apache_Hadoop.html input/file01
hadoop fs -put -f ~/MapReduce.html      input/file02

# run
hadoop fs -rm -r -f url-output
hadoop jar UrlCount.jar UrlCount input url-output

# view top 20 by count
hadoop fs -cat url-output/part-* | sort -k2,2nr | head -20

### 2-worker Timing & Output

- **Elapsed:** 60 seconds

**Top 20 (>5):**
```
mw-data:TemplateStyles:r1238218222	121
mw-data:TemplateStyles:r1295599781	33
/wiki/Doi_(identifier)	18
/wiki/ISBN_(identifier)	18
/wiki/S2CID_(identifier)	14
mw-data:TemplateStyles:r886049734	12
/wiki/MapReduce	7
mw-data:TemplateStyles:r1129693374	7
/wiki/Google_File_System	6
```
