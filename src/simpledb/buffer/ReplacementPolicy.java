package simpledb.buffer;

public enum ReplacementPolicy {
   LRU("Least Recently Used", new LruStrategy()),
   CLOCK("Clock", new ClockStrategy()),
   DEFAULT("Default", new LruStrategy());

   private final String displayName;
   private ReplacementStrategy strategy;


   ReplacementPolicy(String displayName, ReplacementStrategy strategy) {
      this.displayName = displayName;
      this.strategy = strategy;
   }

   public String getDisplayName() {
      return displayName;
   }

   public ReplacementStrategy getStrategy() {
      return strategy;
   }
}
