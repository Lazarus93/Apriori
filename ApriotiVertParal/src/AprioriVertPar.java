/**
* 
* @author J.O
*
*/
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
	 
public class AprioriVertPar{
		
		private ArrayList<int[]> transactions=new ArrayList<int[]>(); // tous les transactions dans le fichier
		private List<int[]> itemsets=new ArrayList<int[]>(); // Current Working itemsets
		private List<Integer> tid=new ArrayList<Integer>(); // liste des id des transactions
		private ArrayList<Integer> verticalItemset= new ArrayList<Integer>();//liste verticale des  1-itemsets
		private ArrayList<ArrayList<Integer>> verticalCopy=new ArrayList<ArrayList<Integer>>();// Copie pour générer l etape k+1
		private ArrayList<ArrayList<Integer>> tidSet= new ArrayList<ArrayList<Integer>>(); // vertical transactions
		private ArrayList<ArrayList<Integer>> tidSetCopy=new ArrayList<ArrayList<Integer>>(); // vertical transactions
		private int min_sup;
		private long startTime;
		private long fileReadTime;
		private int itemCount;
		Boolean moreSets = false;
		
		private static final int NUM_THREADS = 30;
		
		ArrayList<Integer> t1 = new ArrayList<Integer>();
		ArrayList<Integer> t2 = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> tidTemp = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> vertTemp = new ArrayList<ArrayList<Integer>>();
		Set<Integer> canAdd = new HashSet<Integer>(t1);
		ArrayList<Integer> f=new ArrayList<Integer>();
		ArrayList<Integer> s=new ArrayList<Integer>();
		
		public AprioriVertPar(String fileName, double minSupport, int clusters) throws Exception {
			startTime = System.currentTimeMillis();
			transactions = lectureTransactions(fileName);
			fileReadTime = System.currentTimeMillis();
			
			System.out.println("Fichier lu en " + ((fileReadTime - startTime)) + " milseconds, Avec "
					+ itemCount + " items\n");

			double supDouble = minSupport * transactions.size();
			min_sup = (int) supDouble;

			ArrayList<ArrayList<int[]>> partitionedDataset = partition(transactions, clusters);
		      ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		      List<Callable<Void>> workers = new ArrayList<>();
		      for (int i = 0; i < clusters; i++) {
		          workers.add(createWorker(partitionedDataset.get(i)));
		          
		      }
		      try {
		          executor.invokeAll(workers);
		      } catch (InterruptedException e) {
		          e.printStackTrace();
		          System.exit(-1);
		      }
		}
		
		
		              
		private  Callable<Void> createWorker(final ArrayList<int[]> arrayList) {
		      return new Callable<Void>() {

		          @Override
		          public Void call() throws Exception {
		        	 
		        	  createVerticalFormat();
		                 System.out.println();
		               // 1-itemset frequent
		          		creationCandidats();
		          		
		          		System.out.println();	
		          		// 2-itemset frequent
		          		deuxFreq();// min_sup=2

		    			System.out.println();
		   
		          		// k-itemset frequent
		          		kFreq();
		  			
		        	    return null;
		          }
		          
		      };
		  }

						/*createVerticalFormat();
		                 System.out.println();
		               // 1-itemset frequent
		          		creationCandidats();
		          		System.out.println();	
		          		// 2-itemset frequent
		          		deuxFreq();// min_sup=2
		          		
		    			
		    			System.out.println();
		          	
		          		
		          		
		          		// k-itemset frequent
		          		kFreq();*/
		//////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////Methodes/////////////////////////////////////////////////
		/**
		 * 
		 * @param fileName
		 * @return liste des transactions dans le fichier
		 */
		@SuppressWarnings("resource")
		ArrayList<int[]> lectureTransactions(String fileName) {
			ArrayList<int[]> trans = new ArrayList<int[]>();
			//initialisation
			itemsets = new ArrayList<int[]>();
			tid = new ArrayList<Integer>();
			itemCount = 0;
			HashMap<Integer, Integer> uniqueItems = new HashMap<Integer, Integer>(); // temporary
			BufferedReader data_in;
			try {
				data_in = new BufferedReader(new FileReader(fileName));
				int tidCounter = 0; // Compteur des id des transactions
				while (data_in.ready()) {
					String str = data_in.readLine();
					String[] strItems = str.split("\\s+");// Délimiter par espaces
					int[] items_in_trans = new int[strItems.length];
					for (int i = 0; i < strItems.length; i++) {
						// On Parse tous les items dans chaque trans dans un tab
						items_in_trans[i] = Integer.parseInt(strItems[i]);
						int[] item = new int[] { items_in_trans[i] };
						// itemset doit contenir que les items uniques
						if (!uniqueItems.containsKey(items_in_trans[i])) {
							
							uniqueItems.put(items_in_trans[i], items_in_trans[i]);
							itemsets.add(item);
						}
						itemCount++;
					}
					trans.add(items_in_trans);// Chaque item apartient à une transaction, 
					tid.add(tidCounter);	  //Donc, pour chaque item rencontré dans la DB,	
					tidCounter++;			// On doit inrementer l id des trans
				}
				data_in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return trans;
		}

		/**
		 * Créer une DB verticale 
		 */
		public void createVerticalFormat() {
			
			//verticalItemset = new ArrayList<Integer>(); // transactions
			//tidSet = new ArrayList<ArrayList<Integer>>(); // transaction IDs
			int counter;
			for (int[] i : transactions) {
				counter = 0;
				for (int k = 0; k < i.length; k++) {
					if (verticalItemset.contains(i[counter])) {
						tidSet.get(verticalItemset.indexOf(i[counter])).add(
								tid.get(transactions.indexOf(i)));
					} else {
						verticalItemset.add(i[counter]);
						ArrayList<Integer> temp2 = new ArrayList<Integer>();
						temp2.add(tid.get(transactions.indexOf(i)));
						tidSet.add(temp2);
					}
					counter++;
				}
			}
			
			
		}

		private void creationCandidats() {
			itemCount = 0;
			// Support est la longueur de la tidset(vertical transactions)
			for (int i = 0; i < tidSet.size(); i++) {
				if (tidSet.get(i).size() < min_sup) {
					tidSet.get(i).clear();
					verticalItemset.set(i, null);
				} else {
					itemCount++;
				}
			}
			System.out.println("les 1-frequent itemsets trouvés : " + itemCount + ", en  "
					+ (System.currentTimeMillis() - startTime) + " milseconds");
		}

		private void deuxFreq() {
			// copies created to maintain as master in k+1 candidate generation
			tidSetCopy = new ArrayList<ArrayList<Integer>>(); // transaction IDs
			verticalCopy = new ArrayList<ArrayList<Integer>>();
			itemCount = 0;
			for (int i = 0; i < tidSet.size(); i++) {
				for (int k = i + 1; k < tidSet.size(); k++) {
					if (checkIntersect(tidSet.get(i), tidSet.get(k))
							&& (intersect(tidSet.get(i), tidSet.get(k)).size() >= min_sup)) {
						tidSetCopy.add(intersect(tidSet.get(i), tidSet.get(k)));
						ArrayList<Integer> t1 = new ArrayList<Integer>();
						t1.add(verticalItemset.get(i));
						t1.add(verticalItemset.get(k));
						HashSet<Integer> hs = new HashSet<Integer>();
						hs.addAll(t1);
						t1.clear();
						t1.addAll(hs);
						verticalCopy.add(t1);
						itemCount++;
					}
				}
			}
			
			System.out.println("les 2-frequent itemsets trouvés : " + itemCount + ", en "
					+ (System.currentTimeMillis() - startTime) + " milseconds");
			
			System.out.println();
		}
		
		

		private void kFreq() {
			// Des copies créees pour aider à generer k+1 candidats
			ArrayList<Integer> t1 = new ArrayList<Integer>();
			ArrayList<ArrayList<Integer>> tidTemp = new ArrayList<ArrayList<Integer>>();
			ArrayList<ArrayList<Integer>> vertTemp = new ArrayList<ArrayList<Integer>>();
		
			int pass = 2;
			int size = 3;
			do {
				pass++;
				moreSets = false;
				itemCount = 0;
				for (int i = 0; i < tidSetCopy.size(); i++) {
					for (int k = i + 1; k < tidSetCopy.size(); k++) {
						t1 = new ArrayList<Integer>();
						t1.addAll(verticalCopy.get(i));
						t1.addAll(verticalCopy.get(k));
						HashSet<Integer> hs = new HashSet<Integer>();
						hs.addAll(t1);
						t1.clear();
						t1.addAll(hs);
						if (hs.size() == size) {
							if (checkIntersect(tidSetCopy.get(i), tidSetCopy.get(k))
									&& (intersect(tidSetCopy.get(i), tidSetCopy.get(k)).size() >= min_sup)
									&& (!tidTemp.contains(intersect(tidSetCopy.get(i),
											tidSetCopy.get(k))))) {
								tidTemp.add(intersect(tidSetCopy.get(i), tidSetCopy.get(k)));
								vertTemp.add(t1);
								itemCount++;
								moreSets = true;
							}
						}
					}
				}
				tidSetCopy.addAll(tidTemp);
				verticalCopy.addAll(vertTemp);
				
				System.out.println("les "+pass+"-frequent itemsets trouvés : " + itemCount + ", en "
						+ (System.currentTimeMillis() - startTime) + " milseconds");
				System.out.println();
				tidTemp.clear();
				vertTemp.clear();
				size++;
			} while (moreSets == true);
		}

		private static ArrayList<Integer> intersect(ArrayList<Integer> f, ArrayList<Integer> s) {
			ArrayList<Integer> res = new ArrayList<Integer>();
			int i = 0, j = 0;
			while (i != f.size() && j != s.size()) {
				if (f.get(i) < s.get(j)) {
					i++;
				} else if (f.get(i) > s.get(j)) {
					j++;
				} else {
					res.add(f.get(i));
					i++;
					j++;
				}
			}
			return res;
		}
		//Si on trouve un elt en commun, return true
			private Boolean checkIntersect(ArrayList<Integer> t1,ArrayList<Integer> t2){
				 Set<Integer> canAdd = new HashSet<Integer>(t1);
			for (int i : t2){
				if (canAdd.contains(i))
						return true;
			}
			return false;
			}
			
			
			
			
			
			
			
			////////////////////////////////////////////////////////////////////////////////////
private static ArrayList<ArrayList<int[]>> partition(ArrayList<int[]> transactions, int clusters) {
	ArrayList<ArrayList<int[]>> lists = new ArrayList<ArrayList<int[]>>(clusters);
			      for (int i = 0; i < clusters; i++) {
			          lists.add(new ArrayList<int[]>());
			      }
			      for (int i = 0; i < transactions.size(); i++) {
			          lists.get(i % clusters).add(transactions.get(i));
			      }
			      return lists;
			  }


			
			
public static void main(String[] args) {
	// TODO Auto-generated method stub
	
		
	try {
		AprioriVertPar a=new AprioriVertPar("mushroom.dat", 0.4, 1);
		
		
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}


}
	    


