package esoterum.type;

public class BinaryAcceptor extends BinaryBlock {
    public BinaryAcceptor(String name){
        super(name);
        rotate = true;
        drawArrow = true;
        emits = true;
    }

    public class BinaryAcceptorBuild extends BinaryBuild {

        @Override
        public void updateTile(){
            lastSignal = nextSignal;
            nextSignal = signal();
        }
        @Override
        public boolean signal(){
            return sBack() | sLeft() | sRight() | signalOverride;
        }
    }
}
