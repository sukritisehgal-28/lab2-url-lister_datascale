
# Lab 2 — UrlCount 

## What this does
Counts URLs in Wikipedia HTML (matches `href="..."` or `href='...'`) and prints only those with **count > 5**.
This solution describes my approach to running a Hadoop MapReduce job on **Google Cloud Dataproc**, counting URL frequencies. The experiment was performed on two Dataproc cluster configurations:
- *1 master, 2 workers*
- *1 master, 4 workers*

## Software & Environment

- *Google Cloud Dataproc* (Hadoop cluster)
- *Java 11* (MapReduce implementation, compiled with `--release 11`)
- *Hadoop* (cluster default on Dataproc)
- *Input:* Wikipedia HTML of **Apache Hadoop** and **MapReduce** pages


## Resources Used

- Dataproc clusters (1 master, **2 / 4** workers)
- The two Wikipedia pages as input
- WordCount-style MapReduce adapted to extract `href` targets

I used the following commands to run the environment after writing my Urlcount.java

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

###RESULTS

### 2-worker Timing & Output

- **Elapsed:** 60 seconds

**Top 20 (>5):**
```
mw-data:TemplateStyles:r1238218222      121
mw-data:TemplateStyles:r1295599781      33
/wiki/Doi_(identifier)  18
/wiki/ISBN_(identifier) 18
/wiki/S2CID_(identifier)        14
mw-data:TemplateStyles:r886049734       12
/wiki/MapReduce 7
mw-data:TemplateStyles:r1129693374      7
/wiki/Google_File_System        6

```

### 4-worker Timing & Output

- **Elapsed:** 57 seconds

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

##ANALYSIS 
The 4-worker cluster completed the task slightly faster (**57s vs 60s**). This is expected as additional workers increase parallelism, which allows Hadoop to process data to be processed; however, for small inputs, the improvement is average. Ours is not that much faster because hadoop is doing alot of different jobs at once, also the difference is small due to the small data size or overhead. 

Surprising outcomes (possible explanations):
- Speedup isn’t proportional to the number of workers because of Hadoop overhead and startup time.
- For small inputs, adding workers may show little to no benefit, as it should show some difference.
- Different page revisions on Wikipedia can change exact counts/revision IDs over time; what matters is the count > 5 filter.

---

## Combiner in Java WordCount (why it can be a problem here)
Combiner is usually used to improve the efficiency by summing up the results before sending them to the reducer. 

- Combiners are run*0/1/many times on mapper outputs only. Thus, URLs appear many times per mapper, sumup counts locally. 
- If a combiner filters (e.g., drops keys with counts ≤ 5), it can discard URLs that would exceed 5 only after the global reduce stage. So filtering early can throw away URLs that should have been kept.
- Filtering must be done only in the reducer. A sum-only combiner (just adds partial counts without filtering) is safe and can reduce shuffle volume. Thu,s for our urlcount the combiner is safe if it just sums. 









