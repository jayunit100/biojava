package org.biojavax.bio.phylo;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import org.biojavax.bio.phylo.io.nexus.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;


public class DistanceBasedTreeMethod {
	
	private static WeightedGraph<String, DefaultWeightedEdge> jgrapht =  new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	
	public static void Upgma(TaxaBlock t, CharactersBlock ch){
	
		String v1, v2, v3;
		int index_x = 0, index_y = 0, p_index = 0;

      	int NTax = t.getDimensionsNTax();
      	List labels = t.getTaxLabels();
		
		String [] seq = new String[NTax];
		double [][] distance = new double[NTax][];

		for(int i = 0; i < NTax; i++){
			seq[i] = "";
			distance[i] = new double[NTax];
		}

		for (Iterator i = labels.iterator(); i.hasNext(); ) {
			String taxa = (String)i.next();
		      List matrix = ch.getMatrixData(taxa);
			
		      for (Iterator j = matrix.iterator(); j.hasNext(); ) {                     
				Object elem = j.next();
				
				if (elem instanceof Set) {
					// This is a curly-braces {} enclosed
                              // set of values from the matrix.
                              Set data = (Set)elem;
				} else if (elem instanceof List) {
                              // This is a round-braces () enclosed
                              // set of values from the matrix.
                              List data = (List)elem;
				} else {
                              // Assume it's a string.
                              String data = elem.toString();
					  
					if(data != null && data != " ")
						seq[labels.indexOf(taxa)] += data;
				}
	  		}
		}
	
		// build initial distance matrix
		for( int i = 0; i < NTax; i++){
			for(int j = 0; j < NTax; j++){
				if(i == j) 
					distance[i][j] = 0.0;
				else
					distance[i][j] = MultipleHitCorrection.JukesCantor(seq[i], seq[j]);	
			}
		}
		
		do{
			//find minimum distance pair
			double min_d = distance[0][1];
			for( int i = 0; i < NTax; i++){
				for(int j = i + 1; j < NTax; j++){
					if( min_d >= distance[i][j]){ 
						min_d = distance[i][j];
						index_x = i;
						index_y = j;
					}
				}
			}

			// build a sub-tree by using jgrapht
			v1 = (String) labels.get(index_x);
			v2 = "p" + p_index;
			v3 = (String) labels.get(index_y);
			
			jgrapht.addVertex(v1);
			jgrapht.addVertex(v2);
			jgrapht.addVertex(v3);	
			jgrapht.addEdge(v1,v2);
			jgrapht.addEdge(v2,v3);	

			p_index++;
			
			System.out.println(jgrapht.toString());

			//collapse a min_distance pair and re-build distance matrix
			for(int i = 0; i < NTax; i++){
				for(int j = i; j < NTax; j++){
					if(i == j){
						distance[i][j] = 0.0;
					}else if(i == index_x && j == index_y){
						for(int k = j+1; k < NTax; k++){
							distance[i][j] =  (distance[k][i] + distance[k][j])/2;
							distance[j][i] = distance[i][j];
							labels.set(i, (Object) v2);
							labels.set(j, labels.get(k));
						}
						labels.set(NTax-1, (Object) null);
					}else if(j == index_x){
						for(int k = j+1; k < NTax; k++){
							if(k == index_y){
								distance[i][j] =  (distance[i][j] + distance[i][k])/2;
								distance[j][i] = distance[i][j];
								labels.set(index_x, (Object) v2);
								labels.set(index_y, (Object) null);
							}
						}
					}
					
				}
			}
			NTax--;

		//iterate until tree is completed!
		}while(NTax > 1); 		
	}	


	public static void NeighborJoining(TaxaBlock t, CharactersBlock ch){

		String v1, v2, v3;
		int index_x = 0, index_y = 0, p_index = 0;

      	int NTax = t.getDimensionsNTax();
      	List labels = t.getTaxLabels();
		
		String [] seq = new String[NTax];
		double []net_divergence = new double[NTax];
		double [][] raw_distance = new double[NTax][];
		double [][] distance = new double[NTax][];

		for(int i = 0; i < NTax; i++){
			seq[i] = "";
			raw_distance[i] = new double[NTax];
			distance[i] = new double[NTax];
		}

		for (Iterator i = labels.iterator(); i.hasNext(); ) {
			String taxa = (String)i.next();
		      List matrix = ch.getMatrixData(taxa);
			
		      for (Iterator j = matrix.iterator(); j.hasNext(); ) {                     
				Object elem = j.next();
				
				if (elem instanceof Set) {
					// This is a curly-braces {} enclosed
                              // set of values from the matrix.
                              Set data = (Set)elem;
				} else if (elem instanceof List) {
                              // This is a round-braces () enclosed
                              // set of values from the matrix.
                              List data = (List)elem;
				} else {
                              // Assume it's a string.
                              String data = elem.toString();
					  
					if(data != null && data != " ")
						seq[labels.indexOf(taxa)] += data;
				}
	  		}
		}
		
		
	  	// build initial distance matrix
		for( int i = 0; i < NTax; i++){
			for(int j = 0; j< NTax; j++){
				if(i == j) 
					raw_distance[i][j] = 0.0;
				else
					raw_distance[i][j] = MultipleHitCorrection.JukesCantor(seq[i], seq[j]);	
			
				net_divergence[i] =+ raw_distance[i][j];
			}
	  	}
		
		//iterate until tree is completed!
		do{
			// calculate distance matrix from raw_distances & net divergence
			for(int i = 0; i < NTax; i++){
				for(int j = 0; j < NTax; j++){
					if(i == j)
						distance[i][j] = 0.0;
					else
						distance[i][j] = raw_distance[i][j] - ((net_divergence[i] + net_divergence[j])/2) ;
				}
			}
			
			//find minimum distance pair
			double min_d = distance[0][1];
			for( int i = 0; i < NTax; i++){
				for(int j = i + 1; j < NTax; j++){
					if( min_d >= distance[i][j]){ 
						min_d = distance[i][j];
						index_x = i;
						index_y = j;
					}
				}
			}

			// build a sub-tree by using jgrapht
			v1 = (String) labels.get(index_x);
			v2 = "p" + p_index;
			v3 = (String) labels.get(index_y);
			
			jgrapht.addVertex(v1);
			jgrapht.addVertex(v2);
			jgrapht.addVertex(v3);	
			jgrapht.addEdge(v1,v2);
			jgrapht.addEdge(v2,v3);
			
			//adding weight to the edge
			jgrapht.setEdgeWeight(jgrapht.getEdge(v1,v2), ((raw_distance[index_x][index_y]/2) + (net_divergence[index_x] - net_divergence[index_y])/(2*(NTax-2))) );
			jgrapht.setEdgeWeight(jgrapht.getEdge(v2,v3), raw_distance[index_x][index_y] - ((raw_distance[index_x][index_y]/2) + (net_divergence[index_x] - net_divergence[index_y])/(2*(NTax-2))) );		
			
			p_index++;
			
			System.out.println(jgrapht.toString());

			//collapse a min_distance pair and re-build distance matrix
			for(int i = 0; i < NTax; i++){
				for(int j = i; j < NTax; j++){
					if(i == j){
						distance[i][j] = 0.0;
					}else if(i == index_x && j == index_y){
						for(int k = j+1; k < NTax; k++){
							raw_distance[i][j] =  (raw_distance[k][i] + raw_distance[k][j] - raw_distance[index_x][index_y])/2;
							raw_distance[j][i] = raw_distance[i][j];
							labels.set(i, (Object) v2);
							labels.set(j, (Object) labels.get(k));
						}
						labels.set(NTax-1, (Object) null);
					}else if(j == index_x){
						for(int k = j+1; k < NTax; k++){
							if(k == index_y){
								raw_distance[i][j] =  (raw_distance[i][j] + raw_distance[i][k] -raw_distance[index_x][index_y])/2;
								raw_distance[j][i] = raw_distance[i][j];
								labels.set(index_x, (Object) v2);
								labels.set(index_y, (Object) null);
							}
						}
					}
					
				}
			}

			NTax--;

		//iterate until tree is completed!
		}while(NTax > 1); 		
	}	


}

