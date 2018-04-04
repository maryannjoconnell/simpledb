package simpledb.buffer;

import simpledb.file.Block;

public class ClockStrategy implements ReplacementStrategy {
   private int hand = 0; // CS4432-Project1:
   private Buffer[] bufferpool_copy; // CS4432-Project1:
   private int numAvailable;

   public Buffer getBufferToPinNew() {
     return chooseUnpinnedBuffer();
   }

   public void incrementAvailable(Buffer buff) {
      numAvailable++;
   }

   public void decrementAvailable(Buffer buff) {
      numAvailable--;
   }

   public int available() {
      return numAvailable;
   }

   public void initStrategyFields(int numbuffs, Buffer[] bufferpool) {
      numAvailable = numbuffs;
      bufferpool_copy = new Buffer[numbuffs];

      for (int i=0; i<numbuffs; i++) {
         // CS4432-Project1: Initialize ref bit with 0
         bufferpool[i].setRefBit(0);
         // CS4432-Project1: Store reference to Buffer object in local copy of bufferpool
         bufferpool_copy[i] = bufferpool[i];
      }
   }

   @Override
   public void update(Buffer buff) {
      buff.setRefBit(1);
   }

   private Buffer chooseUnpinnedBuffer() {
      if (available() == 0) {
         return null;
      }
      // Look for an unpinned buffer to replace in a maximum of two full clock rotations
      for (int i = 0; i < 2 * bufferpool_copy.length; i++) {
         // Verify clock hand is within bounds
         if (hand >= bufferpool_copy.length) {
            hand = 0; // Restart clock hand
         }

         if (!bufferpool_copy[hand].isPinned()) {
            if (bufferpool_copy[hand].getRefBit() == 0) {
               // increment clock hand before returning unpinned buffer
               hand++;
               return (bufferpool_copy[hand]);
            } else {
               // Set reference bit to 0
               bufferpool_copy[hand].setRefBit(0);
            }
         }
         hand++;
      }

      return null;
   }
}
