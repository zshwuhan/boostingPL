/*
 *   BoostingPL - Scalable and Parallel Boosting with MapReduce 
 *   Copyright (C) 2012  Ranler Cao  findfunaax@gmail.com
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.   
 */

package boostingPL.MR;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import boostingPL.MR.io.ClassifierWritable;

public class AdaBoostPLDriver extends Configured implements Tool {
	  
	@Override
	public int run(String[] args) throws Exception {
		boolean exitStatus = false;
		if (args[0].equals("AdaBoostPL-Train")) {
			exitStatus = runTrainJob(args);
		}
		else if (args[0].equals("AdaBoostPL-Test")) {
			exitStatus = runTestJob(args);
		}
		else {
			System.out.print("Usage: AdaBoostPL-Train | AdaBoostPL-Test");
		}
        return exitStatus == true ? 0 : 1;		
	}
	
	private boolean runTrainJob(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Job job = new Job(getConf(),"BoostingPL: AdaBoostPL Train");
		job.setJarByClass(AdaBoostPLDriver.class);
		job.setMapperClass(AdaBoostPLMapper.class);
		//job.setReducerClass(AdaBoostPLReducer.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(ClassifierWritable.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(ClassifierWritable.class);

		FileInputFormat.addInputPath(job, new Path(args[1]));
		Path output = new Path(args[2]);
		FileSystem fs = output.getFileSystem(getConf());
		if (fs.exists(output)) {
			fs.delete(output, true);
		}
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		SequenceFileOutputFormat.setOutputPath(job, output);		
	
		// TODO set the paras
		job.getConfiguration().set("AdaBoost.numInterations", args[3]);
		
		return job.waitForCompletion(true);
	}

	private boolean runTestJob(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Job job = new Job(getConf(),"BoostingPL: AdaBoostPL Test");
		job.setJarByClass(AdaBoostPLDriver.class);
		job.setMapperClass(AdaBoostPLTestMapper.class);
		job.setReducerClass(AdaBoostPLTestReducer.class);
		job.setOutputFormatClass(NullOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path(args[1]));
		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(NullWritable.class);		

		job.getConfiguration().set("AdaBoost.ClassifiersFile", args[2]);
		
		return job.waitForCompletion(true);		
	}	
	
	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new Configuration(), new AdaBoostPLDriver(), args);
		System.exit(exitCode);
	}
	
}