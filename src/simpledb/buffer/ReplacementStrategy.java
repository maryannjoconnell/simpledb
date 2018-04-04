package simpledb.buffer;

import simpledb.file.Block;

/**
 * CS4432-Project1: Interface containing methods used by BasicBufferMgr that across policies
 * */
interface ReplacementStrategy {
   Buffer getBufferToPinNew();
   void incrementAvailable(Buffer buff);
   void decrementAvailable(Buffer buff);
   int available();
   // Used to initialize fields required for policy
   void initStrategyFields(int numbuffs, Buffer[] bufferpool);
   // Update fields when buffer is accessed or modified
   void update(Buffer buff);
}

