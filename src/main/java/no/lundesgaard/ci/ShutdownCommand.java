package no.lundesgaard.ci;

public class ShutdownCommand extends Command {
    public static final ShutdownCommand INSTANCE = new ShutdownCommand();

    private ShutdownCommand() {
    }

    @Override
    public void execute(Ci ci) {
        throw new UnsupportedOperationException("shutdown command is not executable");
    }

    @Override
    public void validate() {
        throw new UnsupportedOperationException("shutdown command is not executable");
    }

    @Override
    public Type type() {
        return Type.SHUTDOWN;
    }
}
