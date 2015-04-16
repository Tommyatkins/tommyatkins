package com.tommyatkins.algorithm.dichotomy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TryDichotomy {

	public static int search(int[] array, int num) {
		int left = 0, right = array.length;
		int index = (left + right) / 2;
		while (left <= right && index >= left && index <= right) {
			int temp = array[index];
			if (temp == num) {
				return num; // match return
			} else if (temp < num) {
				// move right
				if (left == index)
					break;
				left = index;
			} else if (temp > num) {
				// move left
				if (right == index)
					break;
				right = index;
			}
			index = (left + right) / 2;
		}
		return -1;
	}

	public static void main(String[] args) {
		int size = 30, offset = 13;
		int max = size * offset;
		
		int[] array = new int[size];
		for (int i = 0; i < size; i++) {
			array[i] = (int) ((i + Math.random()) * offset);
		}

		System.out.println(Arrays.toString(array));
		
		List<Integer> all = new ArrayList<Integer>();
		
		for (int i = 0; i < max; i++) {
			int result = search(array, i);
			if (result > -1) {
				all.add(i);
			}
		}

		System.out.println(Arrays.toString(all.toArray(new Integer[all.size()])));
		

	}
}
