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

  // ------------ MAPPER ------------
  public static class UrlMapper extends Mapper<Object, Text, Text, IntWritable> {
    private static final IntWritable ONE = new IntWritable(1);
    private static final Text OUT_KEY = new Text();

    // Match href="..." OR href='...' (case-insensitive); capture value in group(2)
    private static final Pattern HREF_PATTERN =
        Pattern.compile("href\\s*=\\s*([\"'])([^\"']+)\\1", Pattern.CASE_INSENSITIVE);

    @Override
    protected void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {
      String line = value.toString();
      Matcher m = HREF_PATTERN.matcher(line);
      while (m.find()) {
        String url = m.group(2).trim();
        if (url.isEmpty()) continue;

        String low = url.toLowerCase();
        // Skip obvious non-links and noisy anchors
        if (low.startsWith("javascript:") || low.startsWith("mailto:")) continue;
        if (low.equals("#") || low.equals("#top")) continue;

        OUT_KEY.set(url);
        context.write(OUT_KEY, ONE); // <url, 1>
      }
    }
  }

  // ------------ COMBINER (sum only; NO filtering here) ------------
  public static class IntSumCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
    private final IntWritable result = new IntWritable();
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context ctx)
        throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable v : values) sum += v.get();
      result.set(sum);
      ctx.write(key, result);
    }
  }

  // ------------ REDUCER (sum, then keep only count > 5) ------------
  public static class SumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private final IntWritable outVal = new IntWritable();
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable v : values) sum += v.get();
      if (sum > 5) {
        outVal.set(sum);
        context.write(key, outVal);
      }
    }
  }

  // ------------ DRIVER ------------
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: UrlCount <input> <output>");
      System.exit(2);
    }

    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "UrlCount");

    job.setJarByClass(UrlCount.class);
    job.setMapperClass(UrlMapper.class);
    job.setCombinerClass(IntSumCombiner.class);   // sums only
    job.setReducerClass(SumReducer.class);        // applies >5 filter
    job.setNumReduceTasks(1);                     // single output file

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
