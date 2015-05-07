package no.lundesgaard.ci.processor;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import static rx.schedulers.Schedulers.computation;

public class CommandProcessor {
	public static final Logger LOGGER = LoggerFactory.getLogger(CommandProcessor.class);

	private final Ci ci;
	private Observable<Command> observable = observable();
	private Subscription subscription;

	public CommandProcessor(Ci ci) {
		this.ci = ci;
	}

	@SuppressWarnings("Convert2MethodRef")
	private Observable<Command> observable() {
		return Observable
				.<Command>create(subscriber -> onSubscribe(subscriber))
				.subscribeOn(computation());
	}

	private void onSubscribe(Subscriber<? super Command> subscriber) {
		try {
			LOGGER.debug("Command processor started");
			sleep();
			while (!subscriber.isUnsubscribed()) {
				Command nextCommand = nextCommand();
				if (nextCommand != null) {
					subscriber.onNext(nextCommand);
				} else {
					sleep();
				}
			}
		} finally {
			LOGGER.debug("Command processor stopped");
		}
	}

	private Command nextCommand() {
		try {
			return Command.nextFrom(ci.commandsPath);
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Invalid command", e);
			return null;
		}
	}

	public void startSubscription() {
		if (subscription != null && !subscription.isUnsubscribed()) {
			throw new IllegalStateException("Command processor is already started");
		}
		this.subscription = observable.subscribe(this::processCommand);
	}

	private void processCommand(Command command) {
		try {
			command.validate();
			LOGGER.debug("Command accepted: {}", command);
			command.execute(ci);
		} catch (IllegalStateException e) {
			LOGGER.warn("Invalid command: {}", command, e);
		}
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
