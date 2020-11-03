package cecs429.query;

import java.util.ArrayList;
import java.util.List;

import cecs429.index.Posting;


public class Operator 
{
	/**
	 * Perform the "OR merge" on two ordered lists
	 * @param <T>
	 * @param list1 first list
	 * @param list2 second list
	 * @return the union of two input lists
	 */
	public static <T extends Comparable> List<T> orMerge(List<T> list1, List<T> list2)
	{	
		List<T> result = new ArrayList<T>();	
		int i = 0;
        int j = 0;
        
        while (i < list1.size() && j < list2.size()) 
        {
        	if(list1.get(i).compareTo(list2.get(j)) == 0)
        	{
        		result.add(list1.get(i));
        		i++;
        		j++;
        	}
        	else if(list1.get(i).compareTo(list2.get(j)) < 0)
        	{
        		result.add(list1.get(i));
        		i++;
        	}
        	else
        	{
        		result.add(list2.get(j));
        		j++;
        	}
        }
        
        // Append the longer list to the results
        while (i < list1.size())
        {
        	result.add(list1.get(i));
            i++;
        }
        
        while (j < list2.size())
        {
        	result.add(list2.get(j));
        	j++;
        } 

        return result;
	}
	
	
	/**
	 * Perform the "AND merge" on two ordered lists
	 * @param <T>
	 * @param list1 first list
	 * @param list2 second list
	 * @return the intersection of two input lists
	 */
	public static <T extends Comparable> List<T> andMerge(List<T> list1, List<T> list2)
	{
		List<T> result = new ArrayList<T>();
		int i = 0;
        int j = 0;
        
        while (i < list1.size() && j < list2.size()) 
        {
        	if(list1.get(i).compareTo(list2.get(j)) == 0)
        	{
        		result.add(list1.get(i));
        		i++;
        		j++;
        	}
        	else if(list1.get(i).compareTo(list2.get(j)) < 0)
        	{
        		i++;
        	}
        	else
        	{
        		j++;
        	}
        }
        
        return result;
	}
	
	
	/**
	 * Perform the "AND NOT merge" on two ordered lists
	 * @param <T>
	 * @param list
	 * @param notList the list which contains all the elements to be negated
	 * @return the list which contains all the elements that only appears in the list but not the notList
	 */
	public static <T extends Comparable> List<T> notMerge(List<T> list, List<T> notList)
	{
		List<T> result = new ArrayList<T>();
		
		int i = 0;
        int j = 0;
        
        while (i < list.size() && j < notList.size()) 
        {
        	if(list.get(i).compareTo(notList.get(j)) == 0)
        	{
        		i++;
        		j++;
        	}
        	else if(list.get(i).compareTo(notList.get(j)) < 0)
        	{
        		result.add(list.get(i));
        		i++;
        	}
        	else
        	{
        		j++;
        	}
        }
        
        // Append the rest of the list to the result
        while (i < list.size())
        {
        	result.add(list.get(i));
            i++;
        }
        
        return result;
	}

	
	
	public static List<Posting> positionalMerge(List<Posting> list1, List<Posting> list2, int distance)
	{
		List<Posting> result = new ArrayList<Posting>();
		
		int i = 0;
		int j = 0;
		
		while (i < list1.size() && j < list2.size())
		{
			if(list1.get(i).getDocumentId() == list2.get(j).getDocumentId())
			{
				 List<Integer> positions1 = list1.get(i).getPositions();
				 List<Integer> positions2 = list2.get(j).getPositions();
				 List<Integer> temp = new ArrayList<Integer>();
				 
				 int m = 0;
				 int n = 0;
				
				 while(m < positions1.size() && n < positions2.size())
				 {
					 int dis = positions2.get(n) - positions1.get(m);
					 
					 if(dis == distance)
					 {
						 temp.add(positions1.get(m));
						 m++;
						 n++;
					 }	
					 else if(dis > distance)
					 {
						 m++;
					 }
					 else
					 {
						 n++;
					 }
				 }
				 
				 if(temp.size() > 0)
				 {
					 result.add(new Posting(list1.get(i).getDocumentId(), temp));
				 }
				 
				 i++;
				 j++;
			}
			else if (list1.get(i).getDocumentId() < list2.get(j).getDocumentId())
			{
				i++;
			}
			else
			{
				j++;
			}
		}
        
        return result;
	}
}
