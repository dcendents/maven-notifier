package fr.jcgay.maven.notifier;

import com.google.common.base.Stopwatch;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.MavenExecutionResult;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class AbstractCustomEventSpy implements Notifier {

    protected Logger logger;
    protected Configuration configuration;
    private Stopwatch stopwatch = new Stopwatch();

    @Override
    public final void init(EventSpy.Context context) {
        stopwatch.start();
    }

    @Override
    public final void onEvent(MavenExecutionResult event) {
        stopwatch.stop();
        if (stopwatch.elapsedTime(SECONDS) > configuration.getThreshold() || isPersistent()) {
            fireNotification(event);
        } else {
            logger.debug("No notification sent because build ends before threshold: " + configuration.getThreshold() + "s.");
        }
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void onFailWithoutProject(List<Throwable> exceptions) {
        // do nothing
    }

    @Override
    public boolean shouldNotify() {
        return getClass().getName().contains(configuration.getImplementation());
    }

    @Requirement
    public void setConfiguration(ConfigurationParser configuration) {
        this.configuration = configuration.get();
    }

    @Requirement
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setStopwatch(Stopwatch stopwatch) {
        this.stopwatch = stopwatch;
    }

    protected abstract void fireNotification(MavenExecutionResult event);

    protected Status getBuildStatus(MavenExecutionResult result) {
        return result.hasExceptions() ? Status.FAILURE : Status.SUCCESS;
    }

    protected long elapsedTime() {
        return stopwatch.elapsedTime(SECONDS);
    }

    protected boolean isPersistent() {
        return false;
    }
}
