Khoi Doan
Maryann O'Connell

Installation:
- Extract the directory, then import it into IntelliJ. Other IDEs like Eclipse
*may* work, but because the project was created in IntellJ it is recommended that
it is opened there.
- To run, run src/simpledb/server/Startup class. You must first set up run
 configurations however; the server takes 2 arguments:
    the folder name under the home directory to put the database
    the replacement strategy to use, "clock" or "lru" (no quotes).
- Next, you can run SQL clients under studentClient/simpledb/ to begin performing
SQL queries.