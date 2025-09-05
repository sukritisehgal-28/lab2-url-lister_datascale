import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class UrlCount {

  // Mapper: read each input line, find all href="...".
  public static class UrlMapper extends Mapper<Object, Text, Text, IntWritable> {
    private static final IntWritable ONE = new IntWritable(1);
    private static final Text OUT_KEY = new Text();

    // Regex: capture whatever is inside the double quotes after href=
    // Matches: href="SOMETHING"
    private static final Pattern HREF = Pattern.compile("href\\=\"([^\"]+)\"");

    @Override
    protected void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {
      String line = value.toString();
      Matcher m = HREF.matcher(line);
      while (m.find()) {
        String url = m.group(1).trim();
        if (!url.isEmpty()) {
          OUT_KEY.set(url);
          context.write(OUT_KEY, ONE); // emit <url, 1>
        }
      }
    }
  }

  // Reducer: sum all counts per URL and output only if sum > 5.
  public static class SumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private final IntWritable outVal = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable v : values) {
        sum += v.get();
      }
      if (sum > 5) { // only output URLs that appear more than 5 times
        outVal.set(sum);
        context.write(key, outVal);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    // Usage: UrlCount <input> <output>
    if (args.length != 2) {
      System.err.println("Usage: UrlCount <input> <output>");
      System.exit(2);
    }

    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "UrlCount");

    job.setJarByClass(UrlCount.class);
    job.setMapperClass(UrlMapper.class);

    // IMPORTANT: Do NOT set a Combiner here.
    job.setReducerClass(SumReducer.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
