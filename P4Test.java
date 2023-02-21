/**
	Sample main program to test the Scores class.
	
   Expected Output:
   
      WORD EPIC has score 4.00
      WORD ADVENTURE has no score
      REVIEW 'A thrilling performance' has score 3.50
      18 words with score above 3.80
      
      WORD EVERYTHING has score 2.00
      WORD GOOD has score 1.67
      REVIEW 'serious dialogue in a gorgeous setting' has score 2.00
      22 words with score above 3.90
*/

public class P4Test
{
	public static void main(String[] args) 
	{
		Scores reviews1 = new Scores("movieReviews7.txt", 7);
		System.out.println(reviews1.getResults("queries0.txt"));
		
      Scores reviews2 = new Scores("movieReviews20.txt", 20);
		System.out.println(reviews2.getResults("queries1.txt"));

    }
}
