package nl.noscapect.emergentcraft.task;

/** Bridges a model-selected activity to its native multi-tick Minecraft executor and records only executor facts. */
public final class NativeActivityTask extends AbstractActivityTask {
    private final String name; private final int total; private int done;
    public NativeActivityTask(String name,int total){this.name=name;this.total=Math.max(1,total);}
    @Override public String name(){return name;} @Override public double progress(){return Math.min(1.0,(double)done/total);}
    public void advance(int progress){done=Math.max(done,progress);if(done>=total)complete();} public void failPostcondition(String reason){fail(reason);}
    @Override protected void onStart(){} @Override protected void onTick(){}
}
