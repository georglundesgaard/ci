package no.lundesgaard.ci.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

import java.util.NoSuchElementException;
import java.util.Queue;

import static rx.schedulers.Schedulers.computation;

public abstract class ObservableQueue<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ObservableQueue.class);

	private final Queue<T> queue;
	private final Observable<T> observable = observable();

	public ObservableQueue(Queue<T> queue) {
		this.queue = queue;
	}

	public void addItem(T item) {
		this.queue.add(item);
	}

	public Subscription subscribe(Action1<T> onNext) {
		return observable.subscribe(onNext);
	}

	@SuppressWarnings("Convert2MethodRef")
	private Observable<T> observable() {
		return Observable
				.<T>create(subscriber -> onSubscribe(subscriber))
				.subscribeOn(computation());
	}

	private void onSubscribe(Subscriber<? super T> subscriber) {
		try {
			LOGGER.debug("{} listener started", this);
			while (!subscriber.isUnsubscribed()) {
				T nextItem = nextItem();
				if (nextItem != null) {
					subscriber.onNext(nextItem);
				} else {
					sleep();
				}
			}
		} finally {
			LOGGER.debug("{} listener stopped", this);
		}
	}

	private T nextItem() {
		if (queue.isEmpty()) {
			return null;
		}
		try {
			return queue.remove();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	private void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// ignore exception
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
