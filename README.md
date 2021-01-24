# SearchEngine
An application written in Java


## Description
### Indexing
The search-based application operates in two modes: in one mode, it will build a disk index for a specified directory, and in another mode it will process queries over an existing disk index.

### Processing mode
When processing queries, it will also operate in two different modes: boolean retrieval mode, in which queries are treated as boolean equations, and only documents satisfying the entire equation are returned; and ranked retrieval mode, where the top 10 documents that satisfy a given "bag of words" query are selected and presented to the user. The user can select this mode at the beginning of the program.

#### Supported boolean queries
- AND
- OR (+)
- NOT (-)
- NEAR (near/k - selecting documents where the second term appears at most k positions away from the first term)
- WILDCARD (a single token containing one or more * characters)

### Spelling correction
Any time a user runs a query using a term that is either missing from the vocabulary or whose document frequency is below some threshold value, the program will still run the query and give results as normal, but then print a suggested modified query where the possibly misspelled term(s) is replaced by a most-likely correction.


### Classification
The application uses Rocchio and Baysian classification to place a document into a category based on its content,


## Special queries
- :q - exit the program.
- :stem token - take the token string and stem it, then print the stemmed term.
- :index directoryname - index the folder specified by directoryname and then begin querying it, effectively restarting the program.
- :vocab - print the first 1000 terms in the vocabulary of the corpus, sorted alphabetically, one term per line. Then print the count of the total number of vocabulary terms.
- :processor - change the token processor.
- :process term - process the term and print out the processed terms.
- :mode - change the querying mode (boolean retrieval mode or ranked retrieval mode.
