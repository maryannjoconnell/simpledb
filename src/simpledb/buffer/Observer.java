package simpledb.buffer;

// CS4432-Project1: Interface for buffer manager implementation to implement
public interface Observer {
   // CS4432-Project1: Called by observable buffer
   void update(Buffer buff);
}
