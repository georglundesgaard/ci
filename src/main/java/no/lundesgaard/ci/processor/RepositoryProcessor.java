package no.lundesgaard.ci.processor;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.event.RepositoryUpdatedEvent;
import no.lundesgaard.ci.model.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import static rx.schedulers.Schedulers.computation;

public class RepositoryProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryProcessor.class);

	private final Ci ci;
	private final Observable<Repository> observable = observable();
	private Subscription subscription;

	public RepositoryProcessor(Ci ci) {
		this.ci = ci;
	}

	@SuppressWarnings("Convert2MethodRef")
	private Observable<Repository> observable() {
		return Observable
				.<Repository>create(subscriber -> onSubscribe(subscriber))
				.subscribeOn(computation());
	}

	private void onSubscribe(Subscriber<? super Repository> subscriber) {
		try {
			LOGGER.debug("Repository processor started");
			sleep();
			while (!subscriber.isUnsubscribed()) {
				int repositoriesScanned = ci.repositories().scan(ci, subscriber::onNext);
				if (repositoriesScanned > 0) {
					LOGGER.debug("Repositories scanned: {}", repositoriesScanned);
				}
				sleep();
				//
			}
		} finally {
			LOGGER.debug("Repository processor stopped");
		}
	}

	public void startSubscription() {
		if (subscription != null && !subscription.isUnsubscribed()) {
			throw new IllegalStateException("Repositoru processor is already started");
		}
		this.subscription = observable.subscribe(this::processRepositoryUpdated);
	}

	private void processRepositoryUpdated(Repository repository) {
		ci.eventQueue.addItem(new RepositoryUpdatedEvent(repository.name, repository.lastCommitId()));
	}

	public void stopSubscription() {
		if (subscription != null && !subscription.isUnsubscribed()) {
			subscription.unsubscribe();
		}
		this.subscription = null;
	}

	private void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// ignore exception
		}
	}
}
