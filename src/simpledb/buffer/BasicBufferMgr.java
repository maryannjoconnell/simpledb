package simpledb.buffer;

import simpledb.file.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static simpledb.buffer.ReplacementPolicy.DEFAULT;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
class BasicBufferMgr implements Observer {
    private Buffer[] bufferpool;
    private BitSet emptyPool;  // CS4432-Project1: Determines which buffer is empty
    private Map<Block, Buffer> blockPosition;  // CS4432-Project1: Maps a block to the buffer it is in
    private ArrayList<Observable> observableList; // CS4432-Project1: List of observable buffers
    private ReplacementPolicy policy; // CS4432-Project1: Replacement policy enum

    /**
     * CS4432-Project1:
     * Creates a buffer manager having the specified number
     * of buffer slots.
     * This constructor depends on both the {@link FileMgr} and
     * {@link simpledb.log.LogMgr LogMgr} objects
     * that it gets from the class
     * {@link simpledb.server.SimpleDB}.
     * Those objects are created during system initialization.
     * Thus this constructor cannot be called until
     * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
     * is called first.
     * @param numbuffs the number of buffer slots to allocate
     * @param replacementPolicy replacement policy to use
     */
    BasicBufferMgr(int numbuffs, String replacementPolicy) {
        emptyPool = new BitSet(numbuffs);
        blockPosition = new ConcurrentHashMap<>();

        if (replacementPolicy == null || replacementPolicy.isEmpty()) {
            policy = DEFAULT;
        } else {
            policy = Arrays.stream(ReplacementPolicy.values()).filter(p -> p.name().equalsIgnoreCase(replacementPolicy))
                    .findFirst().orElse(DEFAULT);
        }

        bufferpool = new Buffer[numbuffs];
        observableList = new ArrayList<>(numbuffs);
        for (int i=0; i<numbuffs; i++) {
            Buffer buff = new Buffer(this);
            bufferpool[i] = buff;
            observableList.add(buff);
        }

        policy.getStrategy().initStrategyFields(numbuffs, bufferpool);
        StringBuffer sb = new StringBuffer();
        sb.append(policy.getDisplayName()).append(" Policy Activated\n");
        System.out.println(sb.toString());
    }

    /**
     * Flushes the dirty buffers modified by the specified transaction.
     * @param txnum the transaction's id number
     */
    synchronized void flushAll(int txnum) {
        for (Buffer buff : bufferpool)
            if (buff.isModifiedBy(txnum))
                buff.flush();
    }

    /**
     * Pins a buffer to the specified block.
     * If there is already a buffer assigned to that block
     * then that buffer is used;
     * otherwise, an unpinned buffer from the pool is chosen.
     * Returns a null value if there are no available buffers.
     * @param blk a reference to a disk block
     * @return the pinned buffer
     */
    synchronized Buffer pin(Block blk) {
        System.out.println("PIN block=" + blk.toString());
        Buffer buff = findExistingBuffer(blk);
        if (buff == null) {
            buff = findEmptyBuffer();
            if (buff == null)
                buff = policy.getStrategy().getBufferToPinNew();
            if (buff == null)
                return null;
            if (buff.block() != null) blockPosition.remove(buff.block());
            buff.assignToBlock(blk);
            blockPosition.put(blk, buff);
            // Block was previously available so adjust count
            policy.getStrategy().decrementAvailable(buff);
        }
        buff.pin();
        System.out.println(this.toString());
        return buff;
    }

    /**
     * Allocates a new block in the specified file, and
     * pins a buffer to it.
     * Returns null (without allocating the block) if
     * there are no available buffers.
     * @param filename the name of the file
     * @param fmtr a pageformatter object, used to format the new block
     * @return the pinned buffer
     */
    // CS4432-Project1: Access changed from package-private to public to enforce method with interface
    public synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
        System.out.println("PIN new filename=" + filename);
        Buffer buff = findEmptyBuffer();
        if (buff == null)
            buff =  policy.getStrategy().getBufferToPinNew();
        if (buff == null)
            return null;
        if (buff.block() != null) blockPosition.remove(buff.block());
        buff.assignToNew(filename, fmtr);
        blockPosition.put(buff.block(), buff);
        policy.getStrategy().decrementAvailable(buff);
        buff.pin();
        System.out.println(this.toString());
        return buff;
    }

    /**
     * Unpins the specified buffer.
     * @param buff the buffer to be unpinned
     */
    // CS4432-Project1: Access changed from package-private to public to enforce method with interface
    public synchronized void unpin(Buffer buff) {
        System.out.println("UNPIN buff=" + Integer.toString(buff.hashCode()));
        boolean previouslyPinned = buff.isPinned();
        buff.unpin();
        if (previouslyPinned) { // Prevent adjusting count if unpin was called for unpinned block
            policy.getStrategy().incrementAvailable(buff);
        }
        System.out.println(this.toString());
    }

    /**
     * Returns the number of available (i.e. unpinned) buffers.
     * @return the number of available buffers
     */
    int available() {
        return policy.getStrategy().available();
    }

    @Override
    public void update(Buffer buff) {
        policy.getStrategy().update(buff);
    }

    /** CS4432-Project1: finds an empty buffer (one with no data)
     * NOTE: also sets the buffer as non-empty in the bitset because
     * 1. the functions calling this function does not know (or care) where the buffer is
     * 2. the functions calling this function WILL use the buffer
     * if this is not good design we can use a hashmap that
     *    maps a buffer to its index in the pool (which could potentially be useful elsewhere)
     */
    private synchronized Buffer findEmptyBuffer() {
        int i = emptyPool.nextClearBit(0);
        if (i < bufferpool.length) {
            emptyPool.set(i);
            return bufferpool[i];
        }
        else
            return null;
    }

    /** CS4432-Project1: finds the buffer corresponding to the block
     * by querying the map containing such mapping. Can return null if
     * no such buffer exists.
     *
     * @param blk
     * @return
     */

    private Buffer findExistingBuffer(Block blk) {
        // CS4432-Project1: find blocks from a hashmap
        return blockPosition.get(blk);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("BasicBufferMgr {\n");
        sb.append("bufferpool:\n");
        for(Buffer b : bufferpool) {
            sb.append("==> ").append(b.toString()).append("\n");
        }
        sb.append("}").append("\n").append(policy.getStrategy().toString());
        return sb.toString();
    }
}
