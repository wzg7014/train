package com.wzg.train.batch.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TestJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext)  {
        System.out.println("TestJob TEST!");
    }
}
