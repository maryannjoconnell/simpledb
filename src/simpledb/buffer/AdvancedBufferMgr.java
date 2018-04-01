package simpledb.buffer;

import simpledb.file.Block;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdvancedBufferMgr {
    protected Buffer[] bufferpool;
    protected int numAvailable;
    BitSet emptyPool;
    Map<Block, Buffer> blockPosition;

    /**
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
     */
    AdvancedBufferMgr(int numbuffs) {
        bufferpool = new Buffer[numbuffs];
        numAvailable = numbuffs;
        for (int i=0; i<numbuffs; i++)
            bufferpool[i] = new Buffer();
        // CS4432-Project1: initialize the new variables
        emptyPool = new BitSet(numbuffs);
        blockPosition = new ConcurrentHashMap();
    }

    /**
     * Flushes the dirty buffers modified by the specified transaction.
     * @param txnum the transaction's id number
     */
    protected synchronized void flushAll(int txnum) {
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
    protected synchronized Buffer pin(Block blk) {
        Buffer buff = findExistingBuffer(blk);
        if (buff == null) {
            buff = chooseUnpinnedBuffer();
            if (buff == null)
                return null;
            if (buff.block() != null) blockPosition.remove(buff.block());
            buff.assignToBlock(blk);
            // CS4432-Project1: add the block to the hashmap
            blockPosition.put(blk, buff);
        }
        if (!buff.isPinned())
            numAvailable--;
        buff.pin();
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
    protected synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
        Buffer buff = chooseUnpinnedBuffer();
        if (buff == null)
            return null;
        if (buff.block() != null) blockPosition.remove(buff.block());
        buff.assignToNew(filename, fmtr);
        // CS4432-Project1: add the block to the hashmap
        blockPosition.put(buff.block(), buff);
        numAvailable--;
        buff.pin();
        return buff;
    }

    /**
     * Unpins the specified buffer.
     * @param buff the buffer to be unpinned
     */
    protected synchronized void unpin(Buffer buff) {
        buff.unpin();
        if (!buff.isPinned())
            numAvailable++;
    }

    /**
     * Returns the number of available (i.e. unpinned) buffers.
     * @return the number of available buffers
     */
    protected int available() {
        return numAvailable;
    }

    // CS4432-Project1:
    // finds an empty buffer (one with no data)
    // NOTE: also sets the buffer as non-empty in the bitset because
    // 1. the functions calling this function does not know (or care)
    // where the buffer is
    // 2. the functions calling this function WILL use the buffer
    // if this is not good design we can use a hashmap that maps a buffer to
    // its index in the pool (which could actually be useful elsewhere
    private synchronized Buffer findEmptyBuffer() {
        int i = emptyPool.nextClearBit(0);
        if (i < bufferpool.length) {
            emptyPool.set(i);
            return bufferpool[i];
        }
        else
            return null;
    }
    private Buffer findExistingBuffer(Block blk) {
        // CS4432-Project1: find blocks from a hashmap
        return blockPosition.get(blk);
        /*
        for (Buffer buff : bufferpool) {
            Block b = buff.block();
            if (b != null && b.equals(blk))
                return buff;
        }
        return null;
        */
    }

    private Buffer chooseUnpinnedBuffer() {
        // CS4432-Project1: find empty blocks using the above function
        Buffer b = findEmptyBuffer();
        if (b != null) return b;
        // no empty buffers, begin pin replacement policy
        for (Buffer buff : bufferpool)
            if (!buff.isPinned())
                return buff;
        return null;
    }
}
