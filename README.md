# Lab 2 - Convert WordCount to UrlCount

In this lab, you're going to take WordCount (an existing Hadoop application that is extensively described in the [Hadoop tutorial](https://hadoop.apache.org/docs/r3.0.3/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html)) and modify it into UrlCount. You can either approach the lab as native Java-hadoop application or use the [Hadoop streaming API](https://www.michael-noll.com/tutorials/writing-an-hadoop-mapreduce-program-in-python/) to implement the lab in Python.

To Do Before Starting:
+ Read through the Hadoop Tutorial mentioned above. If you're unfamilar with Java, we recommend that you use the Streaming API and use Python. There are other solutions
+ The `dataproc` QwikLab if you haven't already. This will get you familiar with starting a cluster on the Google Cloud Platform.

You should first confirm that you can run the existing WordCount1 program on the Coding environment and (later) on the Google `dataproc` environment. Then, create the URLCount tool using the Coding environment -- use that easier (and cheaper) environment to debug your code. Then, lastly, run your URLCount implementation using Google's `dataproc`.

You will need to use `git` throughout this lab. You're going to first develop your code on Coding, check it into Git, then use the Google `dataproc` environment. You'll get your code on that environment by using `git clone` to checkout your repo.

## Lab setup

We recommend using the the [https://coding.csel.io](https://coding.csel.io) environment for the first steps
of this project.

The Coding environment has a pre-configured Hadoop-3.2.1 environment prepared
if you use the CSCI 4253/5253 programming environment.

To test and evaluate your system, we download two WikiPedia articles. Hadoop accesses files from the HDFS file system, and we've provided a `make prepare` rule to copy the wikipedia articles to HDFS in the `input` directory. When running on the Coding environment, this will use files in your local directory; when using the `dataproc` environment, this will use files on the Hadoop filesystem (HDFS)

The `Makefile` is setup explicitly for the Coding
environment. The makefile contains the following targets:

* Running just `make` compiles and creates the output `WordCount1.jar` file.
* Running `make prepare` copies two files from Wikipedia that are used as reference input. The files are placed in directory `input`.
* Running `make run` executes the `WordCount1` application, leaving the output in directory `output`

If you are using the Streaming API, then:
* Running `make stream` will run your streaming version

Later, when using a full multi-node Hadoop enviornment on `dataproc`, running `make filesystem` will create an HDFS entry for your user id. You will need to do this prior to copying the WikiPedia articles on `dataproc`.


## Developing UrlCount
Before modiying WordCount to UrlCount, you should first insure that you can:
+ Run the Hadoop program (i.e., run WordCount1.java on the two WikiPedia articles).
+ Observe the WordCount output.

The Hadoop tutorial shows you how to run the Java program. When running your program (following the directions in the tutorial), note that the program has been named WordCount1 rather than just WordCount. Again, the `Makefile` has all of this set up for you.

It's completely reasonable to get the Python version working first and then try to get the Java version working. The python version is much slower than the Java version and people in industry would prefer to use Java (or something like Spark).

### Using the Java Version

You should develop a program called `UrlCount.java` that counts the URLs in the WikiPedia pages (URLs are in the format href="*url_here*"). You'll create a `UrlCount` class by starting with a copy of WordCount1. In this program, you'll modify the Mapper to extract URL references from the documents in the input directory. This is more an exercise in using Java than using Hadoop, but it's a good preparatory step for our next assignment.

You should only output URL's that have more than 5 references (i.e. where the count of the URL's would be > 5). Although this sounds like a simple change, there are some implications you'll need to discuss in your solution writeup (see below).

This tutorial (http://www.vogella.com/tutorials/JavaRegularExpressions/article.html ) provides information on using the Java Regular Expression classes (`java.util.regex.Matcher and java.util.regex.Pattern`). It also provides an implementation of a class `LinkGetter` that will, when given a URL, downloads the data from the URL and produce a `List<String>` of the URL's contained therein.

You're going to modify that code to *not* download a URL -- instead, you'll provide a `String` (just like in WordCount) and extract the URL references from each string. Your mapper will then output URL references and the count of those references in the input file, more or less like WordCount.

To do this modification, you'll need to take the `LinkGetter` class, include it in the `UrlCount` class and indicate that it is a "static" class (i.e. `public static class LinkGetter`). You should remove the code that fetches the URL and wraps it in a `BufferReader` -- you already have a string. This will require removing some catch blocks, but you should add a new catch ( `IllegalStateException e`) block because the Regex code throws this exception when a regex is not found. Lastly, the original code would fix relative URL references by prepending the original URL, but you should just omit that step.

Now you should be able to compile & run your code by modifying the Makefile and adding rules for `UrlCount`. When you run it for the sample input (make prepare) you should find about 1,131 URL references in the two documents. Each URL should be followed by the number of times it appears in the two different files (...which were processed by two different mappers...). Most of them will appear twice, but some will appear 3, 4 or 5 times.

### Using Hadoop Streaming
If you're using the Hadoop streaming interface, the streaming tutorial will show you how to run the program. A sample Python version of the WordCount program is provided in files `Mapper.py` and `Reducer.py`.

In this case, you'll be doing the same task (finding all URL's embedded in the input documents) but you'll be doing it in Python and creating `URLMapper.py` and `URLReducer.py` for that task.

You could [find the URL using regular expressions](https://www.geeksforgeeks.org/python-check-url-string/) or other methods. Document what you're doing.

Your code should analyze the input line-by-line just as would be done for the Java progam and the results should be the same.

In this case, the `Mapper` is the more complex software
since you'll be developing the URL regex. Note that you can test your `UrlMapper.py` by feeding it the input file. It's a little harder to test the `UrlReducer.py` because you need to ensure that the reducer sees duplicates adjacent to one another. To a first approximation, the Map/Reduce would be the same as running
```
cat input/file01 input/file02  | python Mapper.py | sort | python Reducer.py 
```

## Running this on a distributed Hadoop Cluster

Once you have your code working on Coding, you're ready to spend some dollars on `dataproc`. Spin up a cluster with one master node and two worker nodes.

Login to the master node and checkout your Git repo on that system. You may need to create additional rules or modify parts of the Makefile to run your application in that environment because they use a different version of Java and Hadoop and there are dependences on the installation paths.

Also, as you go through a edit-debug cycle on `dataproc`, you'll need to remove the old output directory prior to re-running your application. You can do that using `hdfs dfs -rm -r /user/user/output`.

## What to hand in

You should run your program on `dataproc` twice -- once on a cluster with one master and 2 workers and once with one master and 4 workers. Time the execution time of your code using *e.g.*
```
time hadoop jar....
```

You should create a file `SOLUTION.md` that briefly describes your solution and what software is needed for it to run. You should indicate what resources you used and anyone you worked with as described in the course collaboration policy.

Note that there should be four URL's that appear more than 5 times. Your output should match the following.
```
wiki/Doi_(identifier)  17
/wiki/ISBN_(identifier) 18
/wiki/MapReduce 6
mw-data:TemplateStyles:r951705291       107
```

The Java WordCount implementation used a `Combiner` to improve efficiency, but that may cause problems for this application and produce a different output. Explain why this would be the case (even if you didn't implement the Java version).

You should also include a comparison of the 2-node and 4-node execution time. Discuss the execution times and any suprising outcomes.

Commit your code by the due state with the `SOLUTION.md` file and push it to Github. Make certain you can see your results on the Github website.