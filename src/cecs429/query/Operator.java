package cecs429.query;

import java.util.ArrayList;
import java.util.List;

import cecs429.index.Posting;

public class Operator 
{
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
        
        // Append the longer list to the results
        while (i < list.size())
        {
        	result.add(list.get(i));
            i++;
        }
        
        return result;
	}
	
	
}