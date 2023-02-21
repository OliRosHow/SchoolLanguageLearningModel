import java.util.*;
import java.io.*;

/**
This class creates a data set from pre-rated reviews and uses this data to predict the score of a review
that is not rated. This class will also provide means to retrieve the average score of words that apear in
reviews, and present words in a list that are higher than a given score.
*/
public class Scores
{
   private HashMap<String, LinkedList<Double>> scores = new HashMap<>();
   private HashSet<String> excludedWords = new HashSet<>();
   private TreeMap<Double, LinkedList<String>> wordsByAverage = new TreeMap<>();
   
   /**
   Creates an instance of the Scores object that stores the average score of words as they appear in pre-rated reviews
   @param filename the name of the file that you wish to read from to construct the data set
   @param numberOfLines the line you would like to read up to (inclusive)
   */
   public Scores(String filename, int numberOfLines)
   {
      this.initializeExcludedWords();
      this.buildScores(filename, numberOfLines); 
      this.buildScoreByAverage(); 
   }
   /**
   Reads a set of commands WORDSCORE, REVIEWSCORE, and WORDSABOVE; Then executes that command with the following argument on that line of the file
   @param querieFile the name of the file containting the commands and arguments
   @return the cumulatave output from all commands read from the file
   */
   public String getResults(String querieFile)
   {
      try
      {
         Scanner inFile = new Scanner(new File(querieFile));
         Scanner inLine;
         String command;
         StringBuilder results = new StringBuilder();
      
         while(inFile.hasNextLine())
         {
            inLine = new Scanner(inFile.nextLine());
         
            while (inLine.hasNext())
            {  
               command = inLine.next().toUpperCase(); 
               switch(command)
               {
                  
                  case "WORDSCORE":
                     results.append(wordScore(inLine));
                     break;
                  
                  case "REVIEWSCORE":
                     results.append(reviewScore(inLine));
                     break;
                  
                  case "WORDSABOVE":
                     results.append(wordsAbove(inLine));
                     break;
                  
                  default:
                     results.append( "Invalid commmand\n");
               } 
            }
            inLine.close(); 
         }
         inFile.close();
         return results.toString();
      }
      catch (FileNotFoundException obj)
      {
         return "file not found";
      }
   }
   /**
   retrieves the average score of a word read in from a scanner object
   @param in the scanner reading the file for input
   @return the average score of the word read in from the scanner object if it exists; otherwise, return that the word has no score
   */
   private String wordScore(Scanner in)
   {  
      try
      {
         String word = in.next();
      
         if(scores.keySet().contains(word.toLowerCase()))
            return String.format("WORD " + word + " has score %.2f\n", round(average(scores.get(word.toLowerCase()))));
         
         return "WORD " + word + " has no score\n";
      }
      catch(NoSuchElementException obj)
      {
         return "no valid arguement found for WordScore\n";
      }
   }  
   /**
   constructs a review score based on past reviews read
   @param in the scanner reading the review
   @return the calculated average score of the review
   */
   private String reviewScore(Scanner in)
   {
      StringBuilder review = new StringBuilder(); // the string to build the return value
      LinkedList<String> wordList = new LinkedList<>(); // the list of words we will use to gather scores and create our return value
      LinkedList<Double> ratingList = new LinkedList<>();// the resulting found scores from the list of words
      Double reviewScore;
   
      while(in.hasNext())
         wordList.add(in.next());// generate the list of words
      
      review.append("REVIEW '");
      if (wordList.size() != 0)
      {
         for(String word: wordList) // look at each word
         {
            review.append(word + " "); // add it to our return value
            
            if(scores.keySet().contains(word.toLowerCase())) // if the word has a score
               ratingList.add(average(scores.get(word.toLowerCase())));// add its cumulative score to the rating list
         }
         review.deleteCharAt(review.length() - 1); // get rid of the extra space
      }
      reviewScore = average(ratingList);
      if( !reviewScore.isNaN())
         review.append(String.format("' has score %.2f\n", round(reviewScore))); // calculate the average of all words' average score
      else
         review.append("' has no score\n");
      
      return review.toString();
   }
   /**
   counts the number of words above a given target
   @param in the scanner reading in the argument
   @return the number of words above the argument read in from the scanner
   */
   private String wordsAbove(Scanner in)
   {
      try
      {
         Double target = in.nextDouble();   
         NavigableMap<Double, LinkedList<String>> greaterSubMap = wordsByAverage.tailMap(target, false); // generate a map of higher values than our target exclusive  
         int numWordsAbove = 0; 
      
         if (!greaterSubMap.isEmpty()) // if the sub map is not empty
         {   
            Double entry = greaterSubMap.firstEntry().getKey(); // get the first key
            for(int i = 0; i < greaterSubMap.size(); i++)// sum all of the entries in the tree
            {
               numWordsAbove += greaterSubMap.get(entry).size();
               entry = greaterSubMap.higherKey(entry); 
            }
         } 
         else
         {
            numWordsAbove = 0; // if the tree is empty then the sum is 0 
         }
         
         return String.format("%d words with score above %.2f\n", numWordsAbove, target);
      }
      catch (InputMismatchException obj) 
      {
         return "";
      }
      catch (NoSuchElementException obj)
      {
         return "no valid argument found for WordsAbove\n";
      }   
   }
   /**
   rounds to the hundreths place
   @param num the number you would like to round
   @return the rounded number
   */
   private Double round(Double num)
   {
      num *= 100;
      Math.round(num);
      return num/100.0;
   } 
   /**
   Method to initialize the excluded words set
   */
   private void initializeExcludedWords()
   {
      String[] excludedStrings = { "you", "she", "they", "the", "and", "but"};
      
      for(int i = 0; i < excludedStrings.length; i++)
         excludedWords.add(excludedStrings[i]);
   }
   /**
   Method to build the data set based on the given file and line to read up to (inclusive)
   @param filename the name of the file that will build the data for the Score object
   @param numberOfLines the last line that will be read from the file
   */
   private void buildScores(String filename, int numberOfLines)
   {
      try
      {
         Scanner inFile = new Scanner(new File(filename));
         Scanner inLine;
         Double score;
         String word;
         int i = 0;
         while ( inFile.hasNextLine() && i < numberOfLines)
         {  
            inLine = new Scanner(inFile.nextLine());
            try
            {
               score = inLine.nextDouble();
            
               while(inLine.hasNext())
               {
                  word = inLine.next();
                  if (word.length() > 2 && !excludedWords.contains(word.toLowerCase())) // if the length is greater than 2
                  {                                                       // and the word isn't in the excluded set
                     scores.putIfAbsent(word.toLowerCase(), new LinkedList<Double>());// create an empty list if they key doesn't have one   
                     scores.get(word.toLowerCase()).add(score);// append the score to the list associated with the key
                  }
               }
            }
            catch(InputMismatchException obj)
            {
               System.out.print("");// if this exeption is thrown very likly the line read in doesnt have a score
            }
            finally
            {
               inLine.close();
            }
            i++;
         }
         inFile.close();
      }   
      catch(FileNotFoundException obj)
      {
         System.out.println("file not found");
      }
   }
   /**
   method to create a seperate hash map organized by the words' averages
   */
   private void buildScoreByAverage()
   {
      HashSet<String> words = new HashSet<>(); // hash set to hold key set from scores
      words.addAll(scores.keySet()); // add the key set to the hashset containter
      Iterator wordSetIterator = words.iterator(); // create an iterator to look through the key set
      String word;
      Double scoreAverage;
       
      while(wordSetIterator.hasNext())
      {
         word = (String) wordSetIterator.next(); // retrieve the next word from the key set
         scoreAverage = average(scores.get(word)); // calculate the average score of that word and store it
         if (wordsByAverage.get(scoreAverage) == null)
            wordsByAverage.put(scoreAverage, new LinkedList<String>()); // create a linked list if that average isnt there
         wordsByAverage.get(scoreAverage).add(word); // add the word to the linked list under that average
      }
   }
   /**
   calculates the average of a list of doubles
   @param scoreList the list you would like the average of
   @return the average of all the Doubles on the list
   */
   private static double average(LinkedList<Double> scoreList)
   {
      Iterator it = scoreList.listIterator();
      double sum = 0;
      
      while(it.hasNext())
         sum += (Double) it.next();
         
      return sum / scoreList.size();
   }
}