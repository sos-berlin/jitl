package com.sos.jitl.classes.plugin;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultThreadFactory implements ThreadFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultThreadFactory.class);
	
	private static final AtomicInteger poolNumber = new AtomicInteger(1);
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String namePrefix;
	private final String identifier;
	
	public DefaultThreadFactory(String prefix) {
		SecurityManager s = System.getSecurityManager();
		identifier = prefix;
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		namePrefix = identifier +"-pool-"+ poolNumber.getAndIncrement() + "-thread-";
	}

	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
		t.setDaemon(true);
		//if (t.isDaemon()){
		//	t.setDaemon(false);
		//}
		if (t.getPriority() != Thread.NORM_PRIORITY){
			t.setPriority(Thread.NORM_PRIORITY);
		}
		t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LOGGER.error(String.format("[%s] Uncaught Exception %s",identifier,e.toString(), e));
			}
		});
		return t;
	}
}
