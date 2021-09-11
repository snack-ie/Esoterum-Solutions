package esoterum.type;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Point2;
import arc.util.*;
import arc.util.io.*;
import esoterum.content.EsoVars;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class BinaryNode extends BinaryAcceptor{
    public float range;
    public BinaryNode(String name){
        super(name);
        rotate = false;
        configurable = true;
        drawConnection = false;
        emitAllDirections = true;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * 8f, y * 8f, range, Color.white);
    }

    public class BinaryNodeBuild extends BinaryAcceptorBuild {
        public BinaryNodeBuild link = null;
        public boolean linked = false;
        public boolean accepting = false;
        // why doesn't afterRead() get called ;-;
        public boolean linkInit = false;
        public int linkPos = -1;

        @Override
        public void updateTile(){
            // supposed to be in afterRead() but the game doesn't fUCKINg call it
            // gets the linked node after building initialization
            if(!linkInit){
                link = linkPos == -1 ? null : (BinaryNodeBuild) Vars.world.build(linkPos);
                linkInit = true;
            }

            lastSignal = nextSignal;
            if(accepting && link != null) {
                nextSignal = link.signal();
            }else{
                nextSignal = signal();
            }
        }

        @Override
        public boolean signal(){
            return getSignal(nb[0]) | getSignal(nb[1]) | getSignal(nb[2]) | getSignal(nb[3]);
        }

        @Override
        public void draw() {
            super.draw();
            if(link != null && accepting) {
                Draw.z(Layer.power);
                Draw.color(Color.white, EsoVars.connectionColor, lastSignal ? 1f : 0f);
                Lines.stroke(1f);
                Lines.line(x, y, link.x, link.y);
                Fill.circle(x, y, 1.5f);
                Fill.circle(link.x, link.y, 1.5f);

                float time = (Time.time / 60f) % 3f;
                Fill.circle(
                        Mathf.lerp(link.x, x, time / 3f),
                        Mathf.lerp(link.y, y, time / 3f),
                        1.5f
                );
            }
        }

        @Override
        public void drawConfigure() {
            Draw.color(Color.white);
            Draw.z(Layer.overlayUI);
            Lines.stroke(1f);
            Lines.circle(x, y, 5f);
            if(link != null)Lines.circle(link.x, link.y, 5f);
            Drawf.dashCircle(x, y, range, Color.white);
            Draw.reset();
        }

        @Override
        public boolean onConfigureTileTapped(Building other) {
            if(other != null && linkValid(other)){
                BinaryNodeBuild bOther = (BinaryNodeBuild) other;

                if(bOther == link){
                    reset();
                    return true;
                }
                if(linked){
                    reset();
                }
                if(!bOther.linked){
                    link = bOther;
                    link.link = this;
                    link.linked = true;
                    link.accepting = true;
                    linked = true;
                    link.linkPos = pos();
                    linkPos = link.pos();
                    return true;
                }
                return true;
            }
            return other == null;
        }

        public boolean linkValid(Building other){
            return other instanceof BinaryNodeBuild
                    && other.team == team
                    && this != other
                    && Mathf.dst(x, y, other.x, other.y) <= range;
        }

        @Override
        public void onRemoved() {
            // reset
            reset();
        }

        public void reset() {
            if(link != null) {
                link.link = null;
                link.accepting = false;
                link.linked = false;
                link.linkPos = -1;
            }
            accepting = false;
            linked = false;
            link = null;
            linkPos = -1;
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public boolean getSignal(BinaryBlock.BinaryBuild to, BinaryBlock.BinaryBuild from){
            if(from instanceof BinaryNode.BinaryNodeBuild){
                if(((BinaryNode.BinaryNodeBuild) from).link != null) return super.getSignal(to, from);
            } else return super.getSignal(to, from);

            return false;
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision == 1){
                lastSignal = nextSignal = read.bool();
                linked = read.bool();
                accepting = read.bool();

                linkPos = read.i();
                Log.info(Point2.unpack(linkPos));
            }
        }

        @Override
        public void writeAll(Writes write) {
            super.writeAll(write);

            write.bool(nextSignal);
            write.bool(linked);
            write.bool(accepting);

            write.i(linkPos);
        }
    }
}
