package jnu.util;

/**
 * User: fionser
 * Date: 12-12-13
 * Time: 上午8:46
 */
public class BitSet {
    public static final long BASE = 1000000000L;
    public static final long MAXX = 5000000000L;//Maximum eXclusive
    public static final int BUCK_SIZE = 1500 << 6;
    private java.util.BitSet []bucks;
    private long cardinality;
    private final Integer lock;

    public BitSet() {
        long totalBitNr = MAXX - BASE;
        int buckNr = (int)Math.round((double)totalBitNr / BUCK_SIZE);
        bucks = new java.util.BitSet[buckNr];
        cardinality = 0;
        lock = 0;
    }

    public boolean isEmpty() {
        return getNextBit() < 0;
    }

    public boolean testBit(long bit) {
        if (bit >= BASE && bit < MAXX) {
            long frm = bit - BASE;
            int idx = (int)(frm / BUCK_SIZE);
            return bucks[idx] != null && bucks[idx].get((int) (frm % BUCK_SIZE));
        }
        return false;
    }

    public void setBit(long bit) {
        if (bit >= BASE && bit < MAXX) {
            long frm = bit - BASE;
            int idx = (int)(frm / BUCK_SIZE);
            if (bucks[idx] == null) {
                bucks[idx] = new java.util.BitSet(BUCK_SIZE);
            }
            /*若该位原本已经被设置过*/
            if (bucks[idx].get((int)(frm % BUCK_SIZE))) {
                return;
            }
            synchronized (lock) {
                cardinality++;
            }
            bucks[idx].set((int)(frm % BUCK_SIZE));
        }
    }

    public void clearBit(long bit) {
        if (bit >= BASE && bit < MAXX) {
            long frm = bit - BASE;
            int idx = (int)(frm / BUCK_SIZE);
            if (bucks[idx] == null) {
                return;
            }
            /*若该位原本已经是空的*/
            if (!bucks[idx].get((int)(frm % BUCK_SIZE))) {
                return;
            }
            bucks[idx].clear((int)(frm % BUCK_SIZE));
            synchronized (lock) {
                cardinality--;
            }
        }
    }

    public synchronized long getNextBit() {
        long bit = -1;
        int set;
        for (int i = 0; i < bucks.length; i++) {
            if (bucks[i] == null) {
                continue;
            }
            if ((set = bucks[i].nextSetBit(0)) >= 0) {
                bit = (long)i * BUCK_SIZE + set + BASE;
            }
        }
        return bit;
    }

    public long cardinality() {
        return this.cardinality;
    }
}
