package lab.ques52;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapred.lib.MultipleOutputs;

import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class GenerateRowNumberReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text>
{
  private MultipleOutputs multipleOutputs;

  @Override
  public void configure(JobConf conf)
  {
    multipleOutputs = new MultipleOutputs(conf);
  }

  public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter)
      throws IOException
  {
    String[] keyTokens = key.toString().split(", ");
    String fileName = keyTokens[0];
    String extension = keyTokens[0];
    int indexSeparator = fileName.indexOf("."); 
    if (indexSeparator >= 0)
    {
      extension = fileName.substring(indexSeparator);
      fileName = fileName.substring(0, fileName.indexOf("."));
    }
    fileName = fileName.replaceAll("[^a-zA-Z0-9]+", "");
    extension = extension.replaceAll("[^a-zA-Z0-9]+", "");
    OutputCollector collector = multipleOutputs.getCollector(fileName, extension, reporter);

    long rowCount = 0;
    long columnCount = 0;
    while (values.hasNext())
    {
      Text value = values.next();
      collector.collect(new Text(Long.toString(rowCount)), value);
      rowCount = rowCount + 1;
      
      String [] columns = value.toString().trim().split(" "); 
      columnCount = columnCount > columns.length ? columnCount : columns.length;
    }
    
    reporter.getCounter(MatrixMultiplicationOneDriver.ROW_COUNTERS, fileName).setValue(rowCount);
    reporter.getCounter(MatrixMultiplicationOneDriver.COLUMN_COUNTERS, fileName).setValue(columnCount);
  }

  @Override
  public void close() throws IOException
  {
    multipleOutputs.close();
  }
}
