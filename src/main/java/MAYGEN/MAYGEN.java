/**
 * MIT License
 *
 * <p>Copyright (c) 2021 Mehmet Aziz Yirik
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * This is the main cass of MAYGEN project for molecular structure generation for a given input
 * molecular formula.
 *
 * @author Mehmet Aziz Yirik
 */
package MAYGEN;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.group.Permutation;

public class MAYGEN {
    public static int size = 0;
    public static int total = 0;
    public static boolean tsvoutput = false;
    public static boolean writeSDF = false;
    public static int[] ys;
    public static int[] zs;
    public static int[] nonCanonicalIndices = new int[2];
    public static int hIndex = 0;
    public static long count = 0;
    public static int matrixSize = 0;
    public static boolean verbose = false;
    public static boolean callForward = true;
    public static int[] connectivityIndices = new int[2];
    public static boolean learningFromConnectivity = false;
    public static FileWriter outFile;
    public static String formula;
    public static String filedir;
    public static boolean flag = true;
    public static boolean learningFromCanonicalTest = false;
    public static boolean biggest = true;
    public static ArrayList<ArrayList<Permutation>> formerPermutations =
            new ArrayList<ArrayList<Permutation>>();
    public static int[] degrees;
    public static int[] initialPartition;
    public static int[][] partitionList;
    public static ArrayList<String> symbols = new ArrayList<String>();
    public static int[] occurrences;
    public static Map<String, Integer> valences;
    public static int[][] max;
    public static int[][] L;
    public static int[][] C;
    public static int r = 0;
    public static int y = 0;
    public static int z = 0;
    public static String[] symbolArrayCopy;
    public static int[] hydrogens;
    public static int[] nodeLabels;
    public static int graphSize;
    public static List<int[]> oxygenSulfur = new ArrayList<int[]>();
    public static int[] firstDegrees;
    public static int partSize = 0;
    public static int totalHydrogen = 0;
    public static ArrayList<String> firstSymbols = new ArrayList<String>();
    public static int[] firstOccurrences;
    public static boolean callHydrogenDistributor = false;
    public static boolean justH = false;
    public static boolean noHydrogen = false;
    public static int sizePart = 0;
    public static boolean singleAtom = true;
    public static boolean onlyDegree2 = true;
    public static boolean OnSm = true;
    public static int oxygen = 0;
    public static int sulfur = 0;

    static {
        // The atom valences from CDK.
        valences = new HashMap<String, Integer>();

        valences.put("C", 4);
        valences.put("N", 3);
        valences.put("O", 2);
        valences.put("S", 2);
        valences.put("P", 3);
        valences.put("F", 1);
        valences.put("I", 1);
        valences.put("Cl", 1);
        valences.put("Br", 1);
        valences.put("H", 1);
    }

    /** Basic functions */

    /**
     * Permuting two entries of an Integer array.
     *
     * @param array Integer[] array
     * @param i int first index
     * @param j int second index
     * @return int[]
     */
    public static int[] permuteArray(int[] array, int i, int j) {
        int temp = 0;
        temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        return array;
    }

    /**
     * Summing entries of an array.
     *
     * @param array int[]
     * @return int sum
     */
    public static int sum(int[] array) {
        int sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum = sum + array[i];
        }
        return sum;
    }

    /**
     * Summing entries of a list until a given index.
     *
     * @param list List<Integer>
     * @return int sum
     */
    public static int sum(int[] list, int index) {
        int sum = 0;
        for (int i = 0; i <= index; i++) {
            sum += list[i];
        }
        return sum;
    }

    /**
     * Getting the number of atoms' occurrences.
     *
     * @param info String[] atom info
     * @return int
     */
    public static int atomOccurrence(String[] info) {
        int number = 1;
        if (info.length > 1) {
            number = Integer.parseInt(info[1]);
        }
        return number;
    }

    /**
     * Performing the permutation action on an int array.
     *
     * @param array int[] array
     * @param permutation Permutation permutation
     * @return int[]
     */
    public static int[] actArray(int[] array, Permutation permutation) {
        int permLength = permutation.size();
        int newIndex = 0;
        int arrayLength = array.length;
        int[] modified = new int[arrayLength];
        for (int i = 0; i < permLength; i++) {
            newIndex = permutation.get(i);
            modified[newIndex] = array[i];
        }
        return modified;
    }

    /**
     * Values for an id permutation for a given size
     *
     * @param size int permutation size
     * @return int[]
     */
    public static int[] idValues(int size) {
        int[] id = new int[size];
        for (int i = 0; i < size; i++) {
            id[i] = i;
        }
        return id;
    }

    /**
     * Builds id permutation.
     *
     * @param size int Permutation size
     * @return Permutation
     */
    public static Permutation idPermutation(int size) {
        return new Permutation(size);
    }

    /**
     * The initializer function, reading the formula to set the degrees, partition and file
     * directory variables.
     *
     * @param formula String molecular formula
     */
    public static void sortAscending(ArrayList<String> symbols) {
        HashMap<String, Integer> inputs = new HashMap<String, Integer>();
        for (int i = 0; i < symbols.size(); i++) {
            if (inputs.containsKey(symbols.get(i))) {
                Integer count = inputs.get(symbols.get(i)) + 1;
                inputs.put(symbols.get(i), count);
            } else {
                inputs.put(symbols.get(i), 1);
            }
        }

        Set<Entry<String, Integer>> set = inputs.entrySet();
        sort(symbols, set);
    }

    public static ArrayList<String> sort(
            ArrayList<String> symbols, Set<Entry<String, Integer>> set) {
        int index = 0;
        int value = 0;
        ArrayList<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
        Collections.sort(
                list,
                new Comparator<Map.Entry<String, Integer>>() {
                    public int compare(
                            Map.Entry<String, Integer> value1, Map.Entry<String, Integer> value2) {
                        int comparison = (value2.getValue()).compareTo(value1.getValue());
                        if (comparison == 0) {
                            return -(valences.get(value2.getKey()))
                                    .compareTo(valences.get(value1.getKey()));
                        } else {
                            return -(value2.getValue()).compareTo(value1.getValue());
                        }
                    }
                });

        for (Entry<String, Integer> entry : list) {
            value = entry.getValue();
            for (int i = 0; i < value; i++) {
                symbols.set(index + i, entry.getKey());
            }
            index += value;
        }
        return symbols;
    }

    public static void singleAtomCheck(String[] atoms) {
        String[] info=atoms[0].split("(?=[0-9])", 2);
        String symbol=info[0];
        if(atoms.length==1) {
        	if(symbol.equals("H")) {
        		singleAtom=false;
        	}else {
        		if (atomOccurrence(info) > 1) {
                    singleAtom = false;
        		}
        	}
        }else {
        	for (String atom : atoms) {
                info = atom.split("(?=[0-9])", 2);
                symbol = info[0];
                if (!symbol.equals("H")) {
                    if (atomOccurrence(info) > 1) {
                        singleAtom = false;
                        break;
                    }
                }
            }
        }
    }
    

    public static void checkOxygenSulfur(String[] atoms) {
        String[] info;
        String symbol;
        for (String atom : atoms) {
            info = atom.split("(?=[0-9])", 2);
            symbol = info[0];
            if (valences.get(symbol) != 2) {
                onlyDegree2 = false;
                OnSm = false;
                break;
            } else {
                if (symbol.equals("S")) {
                    sulfur = atomOccurrence(info);
                } else if (symbol.equals("O")) {
                    oxygen = atomOccurrence(info);
                }
            }
        }
        if (onlyDegree2) {
            matrixSize = sulfur + oxygen;
            hIndex=matrixSize;
        }
    }

    public static void getSingleAtomVariables() {
        String[] atoms = formula.split("(?=[A-Z])");
        ArrayList<String> symbolList = new ArrayList<String>();
        String[] info;
        int hydrogens = 0;
        String symbol;
        hIndex = 1;
        for (String atom : atoms) {
            info = atom.split("(?=[0-9])", 2);
            symbol = info[0];
            if (!symbol.equals("H")) {
                symbolList.add(symbol);
            } else {
                hydrogens = atomOccurrence(info);
            }
        }
        matrixSize = hydrogens + 1;
        for (int i = 0; i < hydrogens; i++) {
            symbolList.add("H");
        }
        setSymbols(symbolList);
    }

    public static void getSymbolOccurrences() {
        String[] atoms = formula.split("(?=[A-Z])");
        ArrayList<String> symbolList = new ArrayList<String>();
        String[] info;
        int occur = 0;
        int hydrogens = 0;
        String symbol;
        for (String atom : atoms) {
            info = atom.split("(?=[0-9])", 2);
            symbol = info[0];
            if (!symbol.equals("H")) {
                occur = atomOccurrence(info);
                sizePart++;
                for (int i = 0; i < occur; i++) {
                    if (occur != 0) {
                        symbolList.add(symbol);
                        hIndex++;
                    }
                }
            } else {
                hydrogens = atomOccurrence(info);
            }
        }
        sortAscending(symbolList);
        for (int i = 0; i < hydrogens; i++) {
            symbolList.add("H");
        }
        firstOccurrences = getPartition(symbolList);
        matrixSize = sum(firstOccurrences);
        setSymbols(symbolList);
        occurrences = getPartition(symbolList);
        if (hydrogens != 0) {
            totalHydrogen += hydrogens;
            if (hIndex == 1) {
                callHydrogenDistributor = false;
            } else if (hIndex == 0) {
                justH = true;
                callHydrogenDistributor = false;
                hIndex = hydrogens;
                matrixSize=hIndex;
            } else {
                callHydrogenDistributor = true;
            }
        } else {
            callHydrogenDistributor = false;
            noHydrogen = true;
        }
    }

    public static int[] nextCount(
            int index, int i, int size, ArrayList<String> symbols, int[] partition) {
        int count = 1;
        if (i == (size - 1)) {
            partition[index] = 1;
            index++;
        } else {
            for (int j = i + 1; j < size; j++) {
                if (symbols.get(i).equals(symbols.get(j))) {
                    count++;
                    if (j == (size - 1)) {
                        partition[index] = count;
                        index++;
                        break;
                    }
                } else {
                    partition[index] = count;
                    index++;
                    break;
                }
            }
        }
        return new int[] {count, index};
    }

    public static int[] getPartition(ArrayList<String> symbols) {
        int i = 0;
        int[] partition = new int[sizePart + 1];
        int size = symbols.size();
        int next = 0;
        int index = 0;
        int[] result = new int[2];
        while (i < size) {
            result = nextCount(index, i, size, symbols, partition);
            next = (i + result[0]);
            index = result[1];
            if (next == size) {
                break;
            } else {
                i = next;
            }
        }
        return partition;
    }
    /**
     * Setting the firstSymbols and symbols global variables for the initial sorted list of symbols.
     *
     * @param symbolList ArrayList<String> sorted list of atom symbols
     */
    public static String[] symbolArray;

    public static void setSymbols(ArrayList<String> symbolList) {
        symbolArray = new String[matrixSize];
        int index = 0;
        for (String symbol : symbolList) {
            symbolArray[index] = symbol;
            index++;
            if (!firstSymbols.contains(symbol)) {
                firstSymbols.add(symbol);
            }
        }
    }

    private static String normalizeFormula(String formula) {
        String[] from = {"cl", "CL", "c", "n", "o", "s", "p", "f", "i", "br", "BR", "h"};
        String[] to = {"Cl", "Cl", "C", "N", "O", "S", "P", "F", "I", "Br", "Br", "H"};
        return StringUtils.replaceEach(formula, from, to);
    }

    private static String[] validateFormula(String formula) {
        String[] from = {"Cl", "C", "N", "O", "S", "P", "F", "I", "Br", "H"};
        String[] to = {"", "", "", "", "", "", "", "", "", ""};
        String result = StringUtils.replaceEach(formula.replaceAll("[0-9]", ""), from, to);
        return result.isEmpty() ? new String[0] : result.split("");
    }

    /**
     * Checking whether a molecular formula can represent a graph or not.
     *
     * <p>For a graph with n nodes, the sum of all its node degrees should be equal or bigger than
     * 2*(n-1). Thus, the minimum number of nodes.
     *
     * @param formula String molecular formula
     * @return boolean
     */
    public static boolean canBuildIsomer(String formula) {
        boolean canBuildIsomer = true;
        String[] atoms = normalizeFormula(formula).split("(?=[A-Z])");
        String[] info;
        String symbol;
        int occur, valence;
        int size = 0;
        int sum = 0;
        for (String atom : atoms) {
            info = atom.split("(?=[0-9])", 2);
            symbol = info[0];
            valence = valences.get(symbol);
            occur = atomOccurrence(info);
            size += occur;
            sum += (valence * occur);
        }
        total = size;
        if (sum % 2 != 0) {
            canBuildIsomer = false;
        } else if (sum < 2 * (size - 1)) {
            canBuildIsomer = false;
        }
        return canBuildIsomer;
    }

    /** Initial degree arrays are set based on the molecular formula. */
    public static void initialDegrees() {
        firstDegrees = new int[matrixSize];
        int index = 0;
        String symbol;
        int length = firstSymbols.size();
        for (int i = 0; i < length; i++) {
            symbol = firstSymbols.get(i);
            for (int j = 0; j < firstOccurrences[i]; j++) {
                firstDegrees[index] = valences.get(symbol);
                index++;
            }
        }
    }

    /**
     * Checks two int[] arrays are equal with respect to an atom partition.
     *
     * @param array1 int[] first array
     * @param array2 int[] second array
     * @param partition int[] atom partition
     * @return boolean
     * @throws IOException
     */
    public static boolean equalSetCheck(int[] array1, int[] array2, int[] partition)
            throws IOException {
        int[] temp = cloneArray(array2);
        temp = descendingSortWithPartition(temp, partition);
        return equalSetCheck2(partition, array1, temp);
    }

    /**
     * Getting a part of a int array specified by two entry indices
     *
     * @param array int[] array
     * @param begin int beginning index
     * @param end int ending index
     * @return Integer[]
     */
    public static int[] getBlocks(int[] array, int begin, int end) {
        return Arrays.copyOfRange(array, begin, end);
    }

    /**
     * Checks two int[] arrays are equal with respect to an atom partition.
     *
     * @param partition int[] atom partition
     * @param array1 int[] array
     * @param array2 int[] array
     * @return boolean
     * @throws IOException
     */
    public static boolean equalSetCheck2(int[] partition, int[] array1, int[] array2)
            throws IOException {
        boolean check = true;
        int i = 0;
        int limit = findZeros(partition);
        if (partition[size - 1] != 0) {
            for (int d = 0; d < size; d++) {
                if (array1[d] != array2[d]) {
                    check = false;
                    break;
                }
            }
        } else {
            int value = 0;
            for (int s = 0; s < limit; s++) {
                value = partition[s];
                if (compareIndexwise(array1, array2, i, (value + i))) {
                    i = i + value;
                } else {
                    check = false;
                    break;
                }
            }
        }
        return check;
    }

    /**
     * Comparing two int arrays are equal or not for given range of entries.
     *
     * @param array int[] array
     * @param array2 int[] array
     * @param index1 int beginning index
     * @param index2 int last index
     * @return boolean
     */
    public static boolean compareIndexwise(int[] array, int[] array2, int index1, int index2) {
        boolean check = true;
        for (int i = index1; i < index2; i++) {
            if (array[i] != array2[i]) {
                check = false;
                break;
            }
        }
        return check;
    }

    /**
     * Comparing two arrays are equal. The second row's index and entries are permuted based on
     * cycle transposition and given permutation.
     *
     * @param index int row index
     * @param A int[][] adjacency matrix
     * @param cycleTransposition Permutation cycle transposition
     * @param permutation Permutation permutation
     * @return boolean
     */
    public static boolean equalRowsCheck(
            int index, int[][] A, Permutation cycleTransposition, Permutation permutation) {
        boolean check = true;
        int[] canonical = A[index];
        int[] original = A[index];
        int newIndex = findIndex(index, cycleTransposition);
        Permutation pm = permutation.multiply(cycleTransposition);
        original = cloneArray(A[newIndex]);
        original = actArray(original, pm);
        if (!Arrays.equals(canonical, original)) {
            check = false;
        }
        return check;
    }

    /**
     * Sorting entries of a subarray specified by indices.
     *
     * @param array int[] array
     * @param index0 int beginning index
     * @param index1 int last index
     * @return int[]
     */
    public static int[] descendingSort(int[] array, int index0, int index1) {
        int temp = 0;
        for (int i = index0; i < index1; i++) {
            for (int j = i + 1; j < index1; j++) {
                if (array[i] < array[j]) {
                    temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                }
            }
        }
        return array;
    }

    /**
     * Sorting entries of a int array for a given atom partition.
     *
     * @param array int[] array
     * @param partition int[] atom partition
     * @return int[]
     * @throws IOException
     */
    public static int[] descendingSortWithPartition(int[] array, int[] partition)
            throws IOException {
        int i = 0;
        int p = 0;
        int limit = findZeros(partition);
        for (int i1 = 0; i1 < limit; i1++) {
            p = partition[i1];
            array = descendingSort(array, i, i + p);
            i = i + p;
        }
        return array;
    }

    /**
     * Checks two arrays in descending order. The second row is sorted in descending order just to
     * check whether there is a possible permutations making the first row non-maximal.
     *
     * @param index int row index
     * @param array1 int[] array
     * @param array2 int[] array
     * @param partition int[] atom partition
     * @return boolean
     * @throws IOException
     */
    public static boolean biggerCheck(int index, int[] array1, int[] array2, int[] partition)
            throws IOException {
        int[] sorted = cloneArray(array2);
        sorted = descendingSortWithPartition(sorted, partition);
        return descendingOrderUpperMatrixCheck(index, partition, array1, sorted);
    }

    /**
     * Checks whether there is a permutation making the row bigger in descending order.
     *
     * @param index int row index
     * @param A int[][] adjacency matrix
     * @param permutation Permutation permutation
     * @param partition int[] atom partition
     * @throws IOException
     */
    public static void setBiggest(int index, int[][] A, Permutation permutation, int[] partition)
            throws IOException {
        biggest = true;
        int[] check = row2compare(index, A, permutation);
        if (!biggerCheck(index, A[index], check, partition)) {
            biggest = false;
        }
    }

    /**
     * Get indices from "learning from canonical test" method. Here, the entry makes the row
     * non-canonical is detected. Its indices are set to nonCanonicalIndices global variables.
     *
     * @param index int row index
     * @param A int[][] adjacency matrix
     * @param cycles List<Permutation> list of cycle transpositions
     * @param partition int[] atom partition
     * @throws IOException
     */
    public static void getLernenIndices(
            int index, int[][] A, ArrayList<Permutation> cycles, int[] partition)
            throws IOException {
        int[] check = new int[size];
        for (Permutation cycle : cycles) {
            check = row2compare(index, A, cycle);
            if (!biggerCheck(index, A[index], check, partition)) {
                setLernenIndices(index, cycle, A, check, partition);
                break;
            }
        }
    }

    /**
     * Setting the nonCanonicalIndices global variable.
     *
     * @param rowIndex1 int first row index
     * @param cycle Permutation cycle transposition
     * @param A int[][] adjacency matrix
     * @param secondRow int[] second row
     * @param partition int[] atom partition
     * @throws IOException
     */
    public static void setLernenIndices(
            int rowIndex1, Permutation cycle, int[][] A, int[] secondRow, int[] partition)
            throws IOException {
        nonCanonicalIndices = new int[2];
        learningFromCanonicalTest = false;
        int rowIndex2 = cycle.get(rowIndex1);
        Permutation permutation = getNonCanonicalMakerPermutation(secondRow, cycle, partition);
        learningFromCanonicalTest = true;
        nonCanonicalIndices = upperIndex(rowIndex1, rowIndex2, A, permutation);
    }

    /**
     * Calculating the permutation, permuting the second row and making first row non maximal.
     *
     * @param array
     * @param cycle
     * @param partition
     * @return
     * @throws IOException
     */
    public static Permutation getNonCanonicalMakerPermutation(
            int[] array, Permutation cycle, int[] partition) throws IOException {
        int[] sorted = cloneArray(array);
        sorted = descendingSortWithPartition(sorted, partition);
        Permutation permutation = getCanonicalPermutation(sorted, array, partition);
        return permutation.multiply(cycle);
    }

    /**
     * For a row given by index, checking whether it is in maximal form or not. If not, the
     * nonCanonicalIndices is set.
     *
     * @param index int row index
     * @param A int[][] adjacency matrix
     * @param partition int[] partition
     * @return boolean
     * @throws IOException
     */
    public static boolean zero(int[] array) {
        boolean check = false;
        for (int i = 0; i < size; i++) {
            if (array[i] == 0) {
                check = true;
                break;
            }
        }
        return check;
    }

    public static boolean rowDescendingTest(int index, int[][] A, int[] partition)
            throws IOException {
        boolean check = true;
        if (zero(partition)) {
            if (!descendingOrderCheck(partition, A[index])) {
                check = false;
                int[] array = cloneArray(A[index]);
                array = descendingSortWithPartition(array, partition);
                Permutation canonicalPermutation =
                        getCanonicalPermutation(array, A[index], partition);
                learningFromCanonicalTest = true;
                nonCanonicalIndices = upperIndex(index, index, A, canonicalPermutation);
            }
        }
        return check;
    }

    /**
     * By a given permutation, checking which entry is mapped to the index.
     *
     * @param permutation Permutation permutation
     * @param index int entry index in the row
     * @return int
     */
    public static int getPermutedIndex(Permutation permutation, int index) {
        int out = 0;
        for (int i = 0; i < permutation.size(); i++) {
            if (permutation.get(i) == index) {
                out += i;
                break;
            }
        }
        return out;
    }

    /**
     * Looking for the upper limit where the original entry is smaller.
     *
     * @param index int row index
     * @param nextRowIndex int index of the row to compare
     * @param A int[][] adjacency matrix
     * @param permutation Permutation permutation from canonical test
     * @return int[]
     * @throws IOException
     */
    public static int[] limit(int index, int nextRowIndex, int[][] A, Permutation permutation)
            throws IOException {
        int[] original = A[index];
        int[] permuted = A[nextRowIndex];
        int[] limit = new int[2];
        limit[0] = index;
        int newIndex, value, newValue;
        for (int i = index + 1; i < size; i++) {
            newIndex = getPermutedIndex(permutation, i);
            value = original[i];
            newValue = permuted[newIndex];
            if (value != newValue) {
                if (value < newValue) {
                    limit[1] = i;
                }
                break;
            }
        }
        return limit;
    }

    /**
     * Looking for the maximum index where the entry is not zero.
     *
     * @param index int row index
     * @param nextRowIndex int index of the row to compare
     * @param A int[][] adjacency matrix
     * @param permutation Permutation permutation from canonical test
     * @return int[]
     * @throws IOException
     */
    public static int[] lowerIndex(int index, int nextRowIndex, int[][] A, Permutation permutation)
            throws IOException {
        int max = 0;
        int upperLimit = limit(index, nextRowIndex, A, permutation)[1];
        int[] permuted = A[nextRowIndex];
        int newIndex, newValue;
        for (int i = index + 1; i < upperLimit; i++) {
            newIndex = getPermutedIndex(permutation, i);
            newValue = permuted[newIndex];
            if (newValue > 0) {
                if (max < newIndex) {
                    max = newIndex;
                }
            }
        }
        int[] pair = new int[2];
        pair[0] = nextRowIndex;
        pair[1] = max;
        return pair;
    }

    /**
     * We need to calculate upperIndex. First, we need our j index where the original row become
     * smaller, then calculating the lower index. Based on these two values and the value of j in
     * the permutation, we calculate our upper index.
     *
     * <p>This upper index is used for the 'learning from canonical test' method.
     *
     * @param index int row index
     * @param nextRowIndex int index of the row to compare
     * @param A int[][] adjacency matrix
     * @param permutation Permutation permutation from canonical test
     * @return int[]
     * @throws IOException
     */
    public static int[] upperIndex(int index, int nextRowIndex, int[][] A, Permutation permutation)
            throws IOException {
        int[] limit = limit(index, nextRowIndex, A, permutation);
        int[] lowerLimit = lowerIndex(index, nextRowIndex, A, permutation);
        int[] upperLimit = new int[2];
        upperLimit[0] = nextRowIndex;
        upperLimit[1] = getPermutedIndex(permutation, limit[1]);
        int[] maximalIndices = getMaximumPair(upperLimit, getMaximumPair(limit, lowerLimit));
        maximalIndices = maximalIndexWithNonZeroEntry(A, maximalIndices);
        return getTranspose(maximalIndices);
    }

    /**
     * In case if the index' entry is zero, updating the index with next index with non-zero entry.
     *
     * @param A int[][] adjacency matrix
     * @param maximalIndices int[] maximal indices for canonical test
     * @return int[]
     */
    public static int[] maximalIndexWithNonZeroEntry(int[][] A, int[] maximalIndices) {
        int rowIndex = maximalIndices[0];
        int columnIndex = maximalIndices[1];
        if ((columnIndex > rowIndex) && A[rowIndex][columnIndex] != 0) {
            return maximalIndices;
        } else {
            int[] output = new int[2];
            for (int i = columnIndex; i < size; i++) {
                if (A[rowIndex][i] > 0) {
                    output[0] = rowIndex;
                    output[1] = i;
                    break;
                }
            }
            return output;
        }
    }

    /**
     * For an index pair, getting its transpose.
     *
     * @param indices int[] indices
     * @return int[]
     */
    public static int[] getTranspose(int[] indices) {
        int[] out = new int[2];
        if (indices[0] > indices[1]) {
            out[0] = indices[1];
            out[1] = indices[0];
            return out;
        } else {
            return indices;
        }
    }

    /**
     * Between two index pairs, getting the bigger indices.
     *
     * @param a int[] indices
     * @param b int[] indices
     * @return int[]
     */
    public static int[] getMaximumPair(int[] a, int[] b) {
        if (a[0] > b[0]) {
            return a;
        } else if (b[0] > a[0]) {
            return b;
        } else {
            if (a[1] > b[1]) {
                return a;
            } else if (b[1] > a[1]) {
                return b;
            } else {
                return a;
            }
        }
    }

    /**
     * Comparing two arrays for specific range of entries, whether the first array is bigger than
     * the second one or not.
     *
     * @param array1 int[] first array
     * @param array2 int[] second array
     * @param index1 int beginning index
     * @param index2 int last index
     * @return boolean
     */
    public static boolean compare(int[] array1, int[] array2, int index1, int index2) {
        boolean check = true;
        for (int i = index1; i < index2; i++) {
            if (array1[i] != array2[i]) {
                if (array1[i] < array2[i]) {
                    check = false;
                    break;
                } else if (array1[i] > array2[i]) {
                    check = true;
                    break;
                }
            }
        }
        return check;
    }

    /**
     * Checks the first row is bigger than the second row just in the upper matrix.
     *
     * @param index int row index
     * @param partition int[] atom partition
     * @param firstRow int[] first row
     * @param secondRow int[] second row
     * @return boolean
     * @throws IOException
     */
    public static boolean descendingOrderUpperMatrixCheck(
            int index, int[] partition, int[] firstRow, int[] secondRow) throws IOException {
        boolean check = true;
        int i = index + 1;
        int p = 0;
        int limit = findZeros(partition);
        for (int k = index + 1; k < limit; k++) {
            p = partition[k];
            if (descendingOrderCheck(firstRow, i, (i + p))) {
                if (!compareIndexwise(firstRow, secondRow, i, (i + p))) {
                    check = compare(firstRow, secondRow, i, (i + p));
                    break;
                }
                i = i + p;
            } else {
                check = false;
                break;
            }
        }
        return check;
    }

    /**
     * Checks subarray of specified range of entries, the array is descending order or not.
     *
     * @param array int[] array
     * @param f int first index
     * @param l int last index
     * @return boolean
     */
    public static boolean descendingOrderCheck(int[] array, int f, int l) {
        boolean check = true;
        for (int i = f; i < l - 1; i++) {
            if (array[i] < array[i + 1]) {
                check = false;
                break;
            }
        }
        return check;
    }

    /**
     * Checks a int array is in descending order or not with respect to a given atom partition.
     *
     * @param partition int[] atom partition
     * @return boolean
     * @throws IOException
     */
    public static boolean descendingOrderCheck(int[] partition, int[] array) throws IOException {
        boolean check = true;
        int i = 0;
        int value = 0;
        int limit = findZeros(partition);
        for (int s = 0; s < limit; s++) {
            value = partition[s];
            if (!descendingOrderCheck(array, i, (value + i))) {
                check = false;
                break;
            } else {
                i = i + value;
            }
        }
        return check;
    }

    /** ******************************************************************** */

    /** Candidate Matrix Generation Functions */

    /**
     * L; upper triangular matrix like given in 3.2.1. For (i,j), after the index, giving the
     * maximum line capacity.
     *
     * @param degrees int[] valences
     * @return upper triangular matrix
     * @throws IOException
     */
    public static void upperTriangularL() throws IOException {
        L = new int[hIndex][hIndex];
        if (hIndex == 2) {
            for (int i = 0; i < hIndex; i++) {
                for (int j = i + 1; j < hIndex; j++) {
                    L[i][j] = Math.min(degrees[i], Lsum(i, j));
                }
            }
        } else {
            for (int i = 0; i < hIndex; i++) {
                for (int j = i + 1; j < hIndex; j++) {
                    L[i][j] = Math.min(degrees[i], Lsum(i, j + 1));
                }
            }
        }
    }

    /**
     * C; upper triangular matrix like given in 3.2.1. For (i,j), after the index, giving the
     * maximum column capacity.
     *
     * @param degrees int[] valences
     * @return upper triangular matrix
     * @throws IOException
     */
    public static int[][] upperTriangularC() throws IOException {
        C = new int[hIndex][hIndex];
        if (hIndex == 2) {
            for (int i = 0; i < hIndex; i++) {
                for (int j = i + 1; j < hIndex; j++) {
                    C[i][j] = Math.min(degrees[j], Csum(i, j));
                }
            }
        } else {
            for (int i = 0; i < hIndex; i++) {
                for (int j = i + 1; j < hIndex; j++) {
                    C[i][j] = Math.min(degrees[j], Csum(i + 1, j));
                }
            }
        }
        return C;
    }

    /**
     * Summing ith rows entries starting from the jth column.
     *
     * @param i int row index
     * @param j int column index
     * @return
     */
    public static int Lsum(int i, int j) {
        int sum = 0;
        for (int k = j; k < hIndex; k++) {
            sum = sum + max[i][k];
        }
        return sum;
    }

    /**
     * Summing ith column entries starting from the jth row.
     *
     * @param i int column index
     * @param j int row index
     * @return
     */
    public static int Csum(int i, int j) {
        int sum = 0;
        for (int k = i; k < hIndex; k++) {
            sum = sum + max[k][j];
        }
        return sum;
    }

    /** Possible maximal edge multiplicity for the atom pair (i,j). */
    public static void maximalMatrix() {
        max = new int[hIndex][hIndex];
        for (int i = 0; i < hIndex; i++) {
            for (int j = 0; j < hIndex; j++) {
                int di = degrees[i];
                int dj = degrees[j];
                if (i == j) {
                    max[i][j] = 0;
                } else {
                    if (di != dj) {
                        max[i][j] = Math.min(di, dj);
                    } else if (di == dj && i != j) {
                        if (justH) {
                            max[i][j] = (di);
                        } else {
                            if (hIndex == 2) {
                                max[i][j] = (di);
                            } else {
                                if (di != 1) {
                                    
                                    max[i][j] = (di - 1);
                                } else {
                                    
                                    max[i][j] = (di);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialization of global variables for the generate of structures for given degree list.
     *
     * @param degreeList int[] valences
     * @throws IOException
     * @throws CloneNotSupportedException
     * @throws CDKException
     */
    public static void generate(int[] degreeList)
            throws IOException, CloneNotSupportedException, CDKException {
        int[][] A = new int[matrixSize][matrixSize];
        degrees = degreeList;
        flag = true;
        maximalMatrix();
        upperTriangularL();
        upperTriangularC();
        int[] indices = new int[2];
        indices[0] = 0;
        indices[1] = 1;
        callForward = true;
        r = 0;
        y = ys[r];
        z = zs[r];
        while (flag) {
        	nextStep(A, indices);
            if (!flag) {
            	break;
            }
            if (learningFromConnectivity) {
            	indices = connectivityIndices;
                findR(indices);
                int value = indexYZ();
                y = ys[value];
                clearFormers(false, y);
                learningFromConnectivity = false;
                callForward = false;
            } else {
            	if (learningFromCanonicalTest) {
            		indices = successor(nonCanonicalIndices, max.length);
                    findR(indices);
                    learningFromCanonicalTest = false;
                    callForward = false;
            	}
            }
        }
    }

    /**
     * Calculation of the next index pair in a matrix.
     *
     * @param indices int[] index pair.
     * @param size int row length.
     * @return int[]
     */
    public static int[] successor(int[] indices, int size) {
        int i0 = indices[0];
        int i1 = indices[1];
        if (i1 < (size - 1)) {
            indices[0] = i0;
            indices[1] = (i1 + 1);
        } else if (i0 < (size - 2) && i1 == (size - 1)) {
            indices[0] = (i0 + 1);
            indices[1] = (i0 + 2);
        }
        return indices;
    }

    /**
     * Calculation of the former index pair in a matrix.
     *
     * @param indices int[] index pair.
     * @param size int row length.
     * @return int[]
     */
    public static int[] predecessor(int[] indices, int size) {
        int i0 = indices[0];
        int i1 = indices[1];
        if (i0 == i1 - 1) {
            indices[0] = (i0 - 1);
            indices[1] = (size - 1);
        } else {
            indices[0] = i0;
            indices[1] = (i1 - 1);
        }
        return indices;
    }

    /**
     * Calling
     *
     * @param A
     * @param indices
     * @return
     * @throws IOException
     * @throws CloneNotSupportedException
     * @throws CDKException
     */
    public static int[][] nextStep(int[][] A, int[] indices)
            throws IOException, CloneNotSupportedException, CDKException {
        if (callForward) {
            return forward(A, indices);
        } else {
            return backward(A, indices);
        }
    }

    /**
     * After generating matrices, adding the hydrogen with respect to the pre-hydrogen distribution.
     *
     * @param A int[][] adjacency matrix
     * @param index int beginning index for the hydrogen setting
     * @return
     * @throws CloneNotSupportedException
     */
    public static int[][] addHydrogens(int[][] A, int index) throws CloneNotSupportedException {
    	if (singleAtom) {
            int hIndex = index;
            int hydrogen = valences.get(symbolArray[0]);
            for (int j = hIndex; j < hydrogen + hIndex; j++) {
                A[0][j] = 1;
                A[j][0] = 1;
            }
        } else if (callHydrogenDistributor) {
            int hIndex = index;
            int limit = 0;
            int hydrogen = 0;
            for (int i = 0; i < index; i++) {
                hydrogen = hydrogens[i];
                limit = hIndex + hydrogen;
                for (int j = hIndex; j < limit; j++) {
                    A[i][j] = 1;
                    A[j][i] = 1;
                }
                if (hydrogen != 0) {
                    hIndex = hIndex + hydrogen;
                }
            }
        }
        return A;
    }

    /**
     * In backward function, updating the block index.
     *
     * @param r int block index
     * @param indices int[] atom valences
     * @return
     */
    public static void updateR(int[] indices) {
        int y = ys[r];
        int z = zs[r];
        if (indices[0] < y) {
            r--;
        } else if (indices[0] > z) {
            r++;
        }
    }

    public static void findR(int[] indices) throws IOException {
        int block = 0;
        int index = 0;
        int part = 0;
        int rowIndex = indices[0];
        int limit = findZeros(initialPartition);
        outer:
        for (int i = 0; i < limit; i++) {
            part = initialPartition[i];
            if (index <= rowIndex && rowIndex < (index + part)) {
                break outer;
            } else {
                block++;
            }
            index = index + part;
        }
        r = block;
    }
    /**
     * The third line of the backward method in Grund 3.2.3. The criteria to decide which function
     * is needed: forward or backward.
     *
     * @param x the value in the adjacency matrix A[i][j]
     * @param lInverse lInverse value of indices {i,j}
     * @param l
     * @return
     * @throws IOException
     */
    public static boolean backwardCriteria(int x, int lInverse, int l) throws IOException {
        boolean check = false;
        int newX = (x - 1);
        if ((lInverse - newX) <= l) {
            check = true;
        }
        return check;
    }

    /**
     * The third step in Grund 3.2.3.
     *
     * <p>Backward step in the algorithm.
     *
     * @param A int[][] adjacency matrix
     * @param indices int[] indices
     * @throws IOException
     */
    public static int[][] backward(int[][] A, int[] indices) throws IOException {
        int i = indices[0];
        int j = indices[1];

        if (i == 0 && j == 1) {
            flag = false;
        } else {
            indices = predecessor(indices, max.length);
            // UPODAE
            findR(indices);
            i = indices[0];
            j = indices[1];
            int x = A[i][j];
            int l2 = LInverse(i, j, A);
            int c2 = CInverse(i, j, A);

            if (x > 0
                    && (backwardCriteria((x), l2, L[i][j]) && backwardCriteria((x), c2, C[i][j]))) {
                A[i][j] = (x - 1);
                A[j][i] = (x - 1);
                indices = successor(indices, max.length);
                // UOPDATE
                findR(indices);
                callForward = true;
            } else {
                callForward = false;
            }
        }
        return A;
    }

    /**
     * Setting successor indices entry if there is a possible filling.
     *
     * @param A int[][] adjacency matrix
     * @param indices int[] entry indices
     * @return int[][]
     * @throws IOException
     * @throws CloneNotSupportedException
     * @throws CDKException
     */
    public static int[][] forward(int[][] A, int[] indices)
            throws IOException, CloneNotSupportedException, CDKException {
        int i = indices[0];
        int j = indices[1];
        int lInverse = LInverse(i, j, A);
        int cInverse = CInverse(i, j, A);
        int minimumValue = Math.min(max[i][j], Math.min(lInverse, cInverse));
        int maximumValue = maximalEntry(minimumValue, lInverse, L[i][j], cInverse, C[i][j]);
        callForward = true;
        return forward(lInverse, cInverse, maximumValue, i, j, A, indices);
    }

    public static int[][] forward(
            int lInverse, int cInverse, int maximalX, int i, int j, int[][] A, int[] indices)
            throws CloneNotSupportedException, CDKException, IOException {
        if (((lInverse - maximalX) <= L[i][j]) && ((cInverse - maximalX) <= C[i][j])) {
            A[i][j] = maximalX;
            A[j][i] = maximalX;
            if (i == (max.length - 2) && j == (max.length - 1)) {
                if (canonicalTest(A)) {
                    if (connectivityTest(A)) {
                        count++;
                        if (writeSDF) write2SDF(addHydrogens(A, hIndex));
                        callForward = false;
                    } else {
                        callForward = false;
                        learningFromConnectivity = true;
                    }
                } else {
                    if (!learningFromCanonicalTest) {
                        callForward = false;
                    }
                }
            } else {
                int value = indexYZ();
                if (indices[0] == zs[value] && indices[1] == (max.length - 1)) {
                    callForward = canonicalTest(A);
                    if (callForward) {
                        indices = successor(indices, max.length);
                        // update
                        findR(indices);
                    } else {
                        callForward = false;
                    }
                } else {
                    indices = successor(indices, max.length);
                    // update
                    findR(indices);
                    callForward = true;
                }
            }
        } else {
            callForward = false;
        }
        return A;
    }

    /**
     * Calculating the maximal entry for the indices.
     *
     * @param min int minimum of L, C amd maximal matrices for {i,j} indices.
     * @param lInverse int Linverse value of {i,j}
     * @param l int L value of {i,j}
     * @param cInverse int Cinverse value of {i,j}
     * @param c int C value of {i,j}
     * @return int max
     */
    public static int maximalEntry(int min, int lInverse, int l, int cInverse, int c) {
        int max = 0;
        for (int v = min; v >= 0; v--) {
            if (((lInverse - v) <= l) && ((cInverse - v) <= c)) {
                max = max + v;
                break;
            }
        }
        return max;
    }

    /**
     * Calculating the sum of the entries in the ith row until the jth column.
     *
     * @param i int row index
     * @param j int column index
     * @param A int[][] adjacency matrix
     * @return int
     */
    public static int LInverse(int i, int j, int[][] A) {
        int sum = 0;
        if (hIndex == 2) {
            for (int s = 0; s <= j; s++) {
                sum = sum + A[i][s];
            }
        } else {
            for (int s = 0; s < j; s++) {
                sum = sum + A[i][s];
            }
        }
        return degrees[i] - sum;
    }

    /**
     * Calculating the sum of the entries in the jth column until the ith row.
     *
     * @param i int row index
     * @param j int column index
     * @param A int[][] adjacency matrix
     * @return int
     */
    public static int CInverse(int i, int j, int[][] A) {
        int sum = 0;
        if (hIndex == 2) {
            for (int s = 0; s <= i; s++) {
                sum = sum + A[s][j];
            }
        } else {
            for (int s = 0; s < i; s++) {
                sum = sum + A[s][j];
            }
        }
        return degrees[j] - sum;
    }

    /**
     * Based on the new degrees and the former partition, getting the new atom partition.
     *
     * @param degrees int[] new atom valences
     * @return int[]
     */
    public static int[] getPartition(int[] degrees) {
        int[] newPartition = new int[degrees.length];
        int i = 0;
        int p = 0;
        int length = 0;
        if (justH || noHydrogen) {
            length = firstOccurrences.length;
        } else {
            length = firstOccurrences.length - 1;
        }
        int index = 0;
        for (int part = 0; part < length; part++) {
            p = firstOccurrences[part];
            int[] subArray = getBlocks(degrees, i, p + i);
            for (Integer item : getSubPartition(subArray)) {
                newPartition[index] = item;
                index++;
            }
            i = i + p;
        }
        return newPartition;
    }

    /**
     * Calculating the sub partitions for a given group of degrees.
     *
     * @param degrees int[] valences
     * @return int[]
     */
    public static int[] getSubPartition(int[] degrees) {
        int i = 0;
        int size = degrees.length;
        int[] partition = new int[size];
        int next = 0;
        int index = 0;
        int[] result = new int[2];
        while (i < size) {
            result = nextCount(index, i, size, degrees, partition);
            index = result[1];
            next = (i + result[0]);
            if (next == size) {
                break;
            } else {
                i = next;
            }
        }
        return partition;
    }

    /**
     * Counting the occurrence of a value in a degree.
     *
     * @param i int index
     * @param size int number
     * @param degrees int[] valences
     * @param partition int[] partition
     * @return int
     */
    public static int[] nextCount(int index, int i, int size, int[] degrees, int[] partition) {
        int count = 1;
        if (i == (size - 1)) {
            partition[index] = 1;
            index++;
        } else {
            for (int j = i + 1; j < size; j++) {
                if (degrees[i] == degrees[j]) {
                    count++;
                    if (j == (size - 1)) {
                        partition[index] = count;
                        index++;
                        break;
                    }
                } else {
                    partition[index] = count;
                    index++;
                    break;
                }
            }
        }
        return new int[] {count, index};
    }

    public static boolean checkLengthTwoFormula(String[] atoms) {
    	boolean check=true;
    	if(atoms.length==1) {
    		String[] info = atoms[0].split("(?=[0-9])", 2);
    		if(info[1].equals("2")) {
    			if(valences.get(info[0])>3){
    				check=false;
        		}
    		}
    	}
    	return check;
    }
    /**
     * Main function to initialize the global variables and calling the generate function.
     *
     * @throws IOException
     * @throws CDKException
     * @throws CloneNotSupportedException
     */
    public static void run() throws IOException, CDKException, CloneNotSupportedException {
        formula = normalizeFormula(formula);
        String[] unsupportedSymbols = validateFormula(formula);
        if (unsupportedSymbols.length > 0) {
            if (verbose)
                System.out.println(
                        "The input formula consists user defined element types: "
                                + Arrays.stream(unsupportedSymbols)
                                        .collect(Collectors.joining(", ")));
        } else {
            long startTime = System.nanoTime();
            if (verbose) System.out.println("MAYGEN is generating isomers of " + formula + "...");
            if (writeSDF) {
                new File(filedir).mkdirs();
                outFile =
                        new FileWriter(
                                new File(filedir + "/" + normalizeFormula(formula) + ".sdf"));
            }
            String[] atoms = formula.split("(?=[A-Z])");
            if(checkLengthTwoFormula(atoms)) {
                singleAtomCheck(atoms);
                if (singleAtom) {
                    getSingleAtomVariables();
                    singleAtom();
                    if (writeSDF) outFile.close();
                    long endTime = System.nanoTime() - startTime;
                    double seconds = (double) endTime / 1000000000.0;
                    DecimalFormat d = new DecimalFormat(".###");
                    if (verbose) {
                        System.out.println("The number of structures is: " + count);
                        System.out.println("Time: " + d.format(seconds) + " seconds");
                    }
                    if (tsvoutput) {
                        System.out.println(formula + "\t" + count + "\t" + d.format(seconds));
                    }
                }else {
                	checkOxygenSulfur(atoms);
                	if (onlyDegree2) {
                        if (oxygen == 0 || sulfur == 0) {
                            degree2graph();
                            if (writeSDF) outFile.close();
                            long endTime = System.nanoTime() - startTime;
                            double seconds = (double) endTime / 1000000000.0;
                            DecimalFormat d = new DecimalFormat(".###");
                            if (verbose) {
                                System.out.println("The number of structures is: " + count);
                                System.out.println("Time: " + d.format(seconds) + " seconds");
                            }
                            if (tsvoutput) {
                                System.out.println(formula + "\t" + count + "\t" + d.format(seconds));
                            }
                        } else {
                        	distributeSulfurOxygen();
                            if (writeSDF) outFile.close();
                            long endTime = System.nanoTime() - startTime;
                            double seconds = (double) endTime / 1000000000.0;
                            DecimalFormat d = new DecimalFormat(".###");
                            if (verbose) {
                                System.out.println("The number of structures is: " + count);
                                System.out.println("Time: " + d.format(seconds) + " seconds");
                            }
                            if (tsvoutput) {
                                System.out.println(formula + "\t" + count + "\t" + d.format(seconds));
                            }
                        }
                    } else {
                        if (canBuildIsomer(formula)) {
                            getSymbolOccurrences();
                            initialDegrees();
                            structureGenerator();
                            if (writeSDF) outFile.close();
                            long endTime = System.nanoTime() - startTime;
                            double seconds = (double) endTime / 1000000000.0;
                            DecimalFormat d = new DecimalFormat(".###");
                            if (verbose) {
                                System.out.println("The number of structures is: " + count);
                                System.out.println("Time: " + d.format(seconds) + " seconds");
                            }
                            if (tsvoutput) {
                                System.out.println(formula + "\t" + count + "\t" + d.format(seconds));
                            }
                        } else {
                            if (verbose)
                                System.out.println(
                                        "The input formula, "
                                                + formula
                                                + ", does not represent any molecule.");
                        }
                    }
                }
            }else {
            	if (verbose)
                    System.out.println(
                            "The input formula, " + formula + ", does not represent any molecule.");
            	
            }
        }
    }

    /** For several calls of the run function, setting the global variables. */
    public static void clearGlobals() {
    	singleAtom = true;
     	onlyDegree2 = true;
     	OnSm = true;
    	oxygen = 0;
    	sulfur = 0;
    	graphSize=0;
    	callForward = true;
        connectivityIndices = new int[2];
        learningFromCanonicalTest = false;
        learningFromConnectivity = false;
        callHydrogenDistributor = false;
        total = 0;
        totalHydrogen = 0;
        writeSDF = false;
        size = 0;
        sizePart = 0;
        nonCanonicalIndices = new int[2];
        hIndex = 0;
        count = 0;
        matrixSize = 0;
        noHydrogen = false;
        justH = false;
        noHydrogen = false;
        singleAtom = false;
        oxygenSulfur = new ArrayList<int[]>();
        formerPermutations = new ArrayList<ArrayList<Permutation>>();
        partitionList = new int[size + 1][1];
        symbols = new ArrayList<String>();
        occurrences = null;
        symbolArray = null;
        r = 0;
        y = 0;
        z = 0;
        partSize = 0;
        firstSymbols = new ArrayList<String>();
        symbols = new ArrayList<String>();
        firstOccurrences = null;
    }

    /**
     * If there are hydrogens in the formula, calling the hydrogenDistributor. This is the
     * pre-hydrogen distribution. Then, the new list of degrees is defined for each hydrogen
     * distribution.
     *
     * @return List<int[]>
     * @throws CloneNotSupportedException
     */
    public static ArrayList<int[]> distributeHydrogens() throws CloneNotSupportedException {
        ArrayList<int[]> degreeList = new ArrayList<int[]>();
        if (!callHydrogenDistributor) {
            degreeList.add(firstDegrees);
        } else {
            List<int[]> distributions = HydrogenDistributor.run(firstOccurrences, firstDegrees);
            if (hIndex == 2) {
                for (int[] dist : distributions) {
                    int[] newDegree = new int[size];
                    for (int i = 0; i < size; i++) {
                        newDegree[i] = (firstDegrees[i] - dist[i]);
                    }
                    if (newDegree[0] == newDegree[1]) degreeList.add(newDegree);
                }
            } else {
                for (int[] dist : distributions) {
                    int[] newDegree = new int[size];
                    for (int i = 0; i < size; i++) {
                        newDegree[i] = (firstDegrees[i] - dist[i]);
                    }
                    degreeList.add(newDegree);
                }
            }
        }
        return degreeList;
    }

    /**
     * Setting the y and z values for each block. y is the beginning index and z is the last index
     * of a block in the adjacency matrix.
     *
     * @throws IOException
     */
    public static void setYZValues() throws IOException {
        ys = new int[size];
        zs = new int[size];
        int limit = findZeros(initialPartition);
        int value = 0;
        int index = 0;
        int y = 0;
        int z = 0;
        for (int i = 0; i < limit; i++) {
            value = initialPartition[i];
            y = findY(i);
            z = findZ(i);
            for (int j = 0; j < value; j++) {
                ys[index] = y;
                zs[index] = z;
                index++;
            }
        }
    }

    /**
     * For a block index r, calculating its first row index.
     *
     * @param r int block index
     * @return int
     */
    public static int findY(int r) {
        return (sum(initialPartition, (r - 1)));
    }

    /**
     * For a block index r, calculating its last row index.
     *
     * @param r int block index
     * @return int
     */
    public static int findZ(int r) {
        return (sum(initialPartition, r) - 1);
    }

    public static void singleAtom() throws CloneNotSupportedException, CDKException, IOException {
        int[][] A = new int[matrixSize][matrixSize];
        count++;
        if (writeSDF) write2SDF(addHydrogens(A, hIndex));
    }

    /**
     * Calling the generate function for each degree values after the hydrogen distribution.
     *
     * @throws IOException
     * @throws CloneNotSupportedException
     * @throws CDKException
     */
    public static void setHydrogens(int[] degree) {
        hydrogens = new int[size];
        for (int i = 0; i < size; i++) {
            hydrogens[i] = firstDegrees[i] - degree[i];
        }
    }

    public static void structureGenerator()
            throws IOException, CloneNotSupportedException, CDKException {
        if (noHydrogen) {
            size = sum(firstOccurrences, firstOccurrences.length - 1);
        } else if (justH) {
            size = hIndex;
        } else {
            size = sum(firstOccurrences, firstOccurrences.length - 2);
        }
        ArrayList<int[]> newDegrees = distributeHydrogens();
        nonCanonicalIndices = new int[2];
        learningFromCanonicalTest = false;
        learningFromConnectivity = false;
        int[] newPartition;
        for (int[] degree : newDegrees) {
            setHydrogens(degree);
            newPartition = getPartition(degree);
            if (writeSDF) symbolArrayCopy = Arrays.copyOf(symbolArray, symbolArray.length);
            if (writeSDF) {
                sortWithPartition(newPartition, degree, symbolArrayCopy);
            } else {
                sortWithPartition(newPartition, degree, symbolArray);
            }
            partSize = 0;
            nonCanonicalIndices = new int[2];
            connectivityIndices = new int[2];
            learningFromConnectivity = false;
            learningFromCanonicalTest = false;
            partitionList = new int[size + 1][1];
            formerPermutations.clear();
            partSize += (findZeros(initialPartition) - 1);
            setYZValues();
            partitionList[0] = initialPartition;
            generate(degree);
        }
    }

    /** 3.6.2. Connectivity Test */

    /**
     * Finding the neighbors of a given index.
     *
     * @param index int row (atom) index
     * @param total int number of atoms.
     * @param mat int[][] adjacency matrix
     * @return Set<Integer>
     */
    public static Set<Integer> nValues(int index, int total, int[][] mat) {
        Set<Integer> nValues = new HashSet<Integer>();
        nValues.add(index);
        int[] theRow = mat[index];
        for (int i = (index + 1); i < total; i++) {
            if (theRow[i] > 0) {
                nValues.add(i);
            }
        }
        return nValues;
    }

    /**
     * Finding the W values of neighbors in the former connectivity partition.
     *
     * @param nValues Set<Integer> N values
     * @param Kformer int[] the K values of the former step
     * @return Set<Integer>
     */
    public static Set<Integer> wValues(Set<Integer> nValues, int[] Kformer) {
        Set<Integer> wValues = new HashSet<Integer>();
        for (Integer i : nValues) {
            wValues.add(Kformer[i]);
        }
        return wValues;
    }

    /**
     * Finding the connectivity partition, so the smallest index in the neighborhood.
     *
     * @param wValues Set<Integer> wValues
     * @param kFormer int[] the K values of the former step
     * @return int[]
     */
    public static int[] kValues(int total, Set<Integer> wValues, int[] kFormer) {
        int[] kValues = new int[total];
        int min = Collections.min(wValues);
        for (int i = 0; i < total; i++) {
            if (wValues.contains(kFormer[i])) {
                kValues[i] = min;
            } else {
                kValues[i] = kFormer[i];
            }
        }
        return kValues;
    }

    /**
     * Initializing the first connectivity partition.
     *
     * @param total int number of atoms.
     * @return int[]
     */
    public static int[] initialKList(int total) {
        int[] k = new int[total];
        for (int i = 0; i < total; i++) {
            k[i] = i;
        }
        return k;
    }

    /**
     * Test whether an adjacency matrix is connected or disconnected.
     *
     * @param mat int[][] adjacency matrix
     * @return boolean
     * @throws IOException
     */
    public static boolean connectivityTest(int[][] mat) throws IOException {
        learningFromConnectivity = false;
        boolean check = false;
        int[] kValues = initialKList(hIndex);
        Set<Integer> nValues = new HashSet<Integer>();
        Set<Integer> wValues = new HashSet<Integer>();
        Set<Integer> zValues = new HashSet<Integer>();
        int zValue = 0;
        for (int i = 0; i < hIndex; i++) {
            nValues = nValues(i, hIndex, mat);
            wValues = wValues(nValues, kValues);
            zValue = Collections.min(wValues);
            zValues.add(zValue);
            kValues = kValues(hIndex, wValues, kValues);
        }
        if (zValue == 0 && allIs0(kValues)) {
            check = true;
        } else {
            setLearningFromConnectivity(zValues, kValues);
        }
        return check;
    }

    /**
     * If matrix is not connected, setting learninfFromConnectivity global variables.
     *
     * @param zValues Set<Integer> minimum index values of each atom's neighborhoods.
     * @param kValues int[] connectivity partition
     */
    public static void setLearningFromConnectivity(Set<Integer> zValues, int[] kValues) {
        learningFromConnectivity = true;
        connectivityIndices[0] = minComponentIndex(zValues, kValues);
        connectivityIndices[1] = hIndex - 1;
    }

    /**
     * Getting the minimum component index. Here, components are compared based on their last
     * indices and sizes.
     *
     * @param zValues Set<Integer> minimum index values of each atom's neighborhoods.
     * @param kValues int[] connectivity partition
     * @return int
     */
    public static int minComponentIndex(Set<Integer> zValues, int[] kValues) {
        int index = findMaximalIndexInComponent(kValues, 0);
        int value = hIndex;
        for (Integer i : zValues) {
            value = findMaximalIndexInComponent(kValues, i);
            if (value < index) {
                index = value;
            }
        }
        return index;
    }

    /**
     * Finding the maximal index in a component to compare with other components.
     *
     * @param kValues int[] connectivity partition
     * @param value int minimum neighborhood index
     * @return int
     */
    public static int findMaximalIndexInComponent(int[] kValues, int value) {
        int maxIndex = hIndex;
        for (int i = hIndex - 1; i > 0; i--) {
            if (kValues[i] == value) {
                maxIndex = i;
                break;
            }
        }
        return maxIndex;
    }

    /**
     * Checks whether all the entries are equal to 0 or not.
     *
     * @param list int[]
     * @return boolean
     */
    public static boolean allIs0(int[] list) {
        boolean check = true;
        for (int i = 0; i < list.length; i++) {
            if (list[i] != 0) {
                check = false;
                break;
            }
        }
        return check;
    }

    /**
     * Based on the molecules automorphisms, testing an adjacency matrix is canonical or not.
     *
     * @param A int[][] adjacency matrix
     * @return boolean
     * @throws IOException
     */
    public static int indexYZ() {
        int index = 0;
        for (int i = 0; i <= r; i++) {
            index = index + initialPartition[i];
        }
        return index - 1;
    }

    public static boolean canonicalTest(int[][] A) throws IOException {
        boolean check = true;
        learningFromCanonicalTest = false;
        int value = indexYZ();
        y = ys[value];
        z = zs[value];
        if (partSize == r && z != 1) {
            z = z - 1;
        }
        clearFormers(false, y);
        boolean test = true;
        for (int i = y; i <= z; i++) {
            test =
                    rowCanonicalTest(
                            i, r, A, partitionList[i], canonicalPartition(i, partitionList[i]));
            if (!test) {
                check = false;
                break;
            }
        }
        clearFormers(check, y);
        return check;
    }

    /**
     * When an adjacency matrix is non-canonical, cleaning the formerPermutations and partitionList
     * from the first row of the tested block.
     *
     * @param check boolean canonical test result
     * @param y int first row of the tested block
     */
    public static void clearFormers(boolean check, int y) {
        if (check == false) {
            int formerSize = formerPermutations.size() - 1;
            for (int i = formerSize; i > (y - 1); i--) {
                formerPermutations.remove(i);
            }

            int partitionSize = partitionList.length - 1;
            for (int i = partitionSize; i > y; i--) {
                partitionList[i] = null;
            }
        }
    }

    /**
     * Calculating all candidate permutations for row canonical test.
     *
     * <p>The DFS multiplication of former automorphisms list with the list of cycle transpositions
     * of the row.
     *
     * @param index int row index
     * @param cycles List<Permutation> cycle transpositions
     */
    public static void candidatePermutations(int index, ArrayList<Permutation> cycles) {
        ArrayList<Permutation> newList = new ArrayList<Permutation>();
        for (Permutation cycle : cycles) {
            newList.add(cycle);
        }
        if (index != 0) {
            ArrayList<Permutation> formers = formerPermutations.get(index - 1);
            for (Permutation form : formers) {
                if (!form.isIdentity()) {
                    newList.add(form);
                }
            }
            ArrayList<Permutation> newForm = new ArrayList<Permutation>();
            for (Permutation frm : formers) {
                if (!frm.isIdentity()) {
                    newForm.add(frm);
                }
            }
            ArrayList<Permutation> newCycles = new ArrayList<Permutation>();
            if (cycles.size() != 1) {
                for (Permutation cyc : cycles) {
                    if (!cyc.isIdentity()) {
                        newCycles.add(cyc);
                    }
                }
            }
            for (Permutation perm : newForm) {
                for (Permutation cycle : newCycles) {
                    Permutation newPermutation = cycle.multiply(perm);
                    if (!newPermutation.isIdentity()) {
                        newList.add(newPermutation);
                    }
                }
            }
        }
        formerPermutations.add(index, newList);
    }

    /**
     * Canonical test for a row in the tested block.
     *
     * @param index int row index
     * @param r int block index
     * @param A int[][] adjacency matrix
     * @param partition int[] former partition
     * @param newPartition int[] canonical partition
     * @return boolean
     * @throws IOException
     */
    public static boolean rowCanonicalTest(
            int index, int r, int[][] A, int[] partition, int[] newPartition) throws IOException {
        boolean check = true;
        if (!rowDescendingTest(index, A, newPartition)) {
            check = false;
        } else {
            int value = indexYZ();
            y = ys[value];
            ArrayList<Permutation> cycles = new ArrayList<Permutation>();
            if (partition[size - 1] != 0) {
                Permutation id = new Permutation(size);
                cycles.add(id);
            } else {
                cycles = cycleTranspositions(index, partition);
            }
            candidatePermutations(index, cycles);
            check = check(index, y, size, A, newPartition);
            if (!check) {
                if (cycles.size() != 1) {
                    getLernenIndices(index, A, cycles, newPartition);
                }
            } else {
                addPartition(index, newPartition, A);
            }
        }
        return check;
    }

    /**
     * Updating canonical partition list.
     *
     * @param index row index
     * @param newPartition atom partition
     * @param A int[][] adjacency matrix
     * @throws IOException
     */
    public static void addPartition(int index, int[] newPartition, int[][] A) throws IOException {
        if (newPartition[size - 1] != 0) {
            partitionList[index + 1] = newPartition;
        } else {
            partitionList[index + 1] = refinedPartitioning(newPartition, A[index]);
        }
    }

    /**
     * Refining the input partition based on the row entries.
     *
     * @param partition int[] atom partition
     * @param row int[] row
     * @return int[]
     * @throws IOException
     */
    public static int[] refinedPartitioning(int[] partition, int[] row) throws IOException {
        int[] refined = new int[size];
        int index = 0;
        int count = 1;
        int refinedIndex = 0;
        int limit = findZeros(partition);
        for (int s = 0; s < limit; s++) {
            if (partition[s] != 1) {
                for (int i = index; i < partition[s] + index - 1; i++) {
                    if (i + 1 < partition[s] + index - 1) {
                        if (row[i] == row[i + 1]) {
                            count++;
                        } else {
                            refined[refinedIndex] = count;
                            refinedIndex++;
                            count = 1;
                        }
                    } else {
                        if (row[i] == row[i + 1]) {
                            count++;
                            refined[refinedIndex] = count;
                            refinedIndex++;
                            count = 1;
                        } else {
                            refined[refinedIndex] = count;
                            refinedIndex++;
                            refined[refinedIndex] = 1;
                            refinedIndex++;
                            count = 1;
                        }
                    }
                }
                index = index + partition[s];
            } else {
                index++;
                refined[refinedIndex] = 1;
                refinedIndex++;
                count = 1;
            }
        }
        return refined;
    }

    /**
     * For a row given by index, detecting the other row to compare in the block. For the detection
     * of the next row index, cycle transposition is used.
     *
     * @param index int row index
     * @param A int[][] adjacency matrix
     * @param cycleTransposition Permutation cycle transposition
     * @return int[]
     */
    public static int[] row2compare(int index, int[][] A, Permutation cycleTransposition) {
        int[] array = cloneArray(A[findIndex(index, cycleTransposition)]);
        array = actArray(array, cycleTransposition);
        return array;
    }

    /**
     * With the cycle permutation, mapping the row index to another row in the block.
     *
     * @param index int row index
     * @param cycle Permutation cycle transposition
     * @return int
     */
    public static int findIndex(int index, Permutation cycle) {
        int size = cycle.size();
        int output = 0;
        for (int i = 0; i < size; i++) {
            if (cycle.get(i) == index) {
                output = i;
                break;
            }
        }
        return output;
    }

    /**
     * Cloning int array
     *
     * @param array int[] array
     * @return int[]
     */
    public static int[] cloneArray(int[] array) {
        return array.clone();
    }

    /**
     * Calculating the canonical permutation of a row.
     *
     * <p>In a block, the original and the other rows are compared; if there is a permutation
     * mapping rows to each other, canonical permutation, else id permutation is returned.
     *
     * @param originalRow int[] original row
     * @param rowToCheck int[] row to compare with
     * @param partition int[] partition
     * @return Permutation
     * @throws IOException
     */
    public static Permutation getCanonicalPermutation(
            int[] originalRow, int[] rowToCheck, int[] partition) throws IOException {
        int[] cycles = getCanonicalPermutation2(partition, originalRow, rowToCheck);
        int[] perm = new int[size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == cycles[j]) {
                    perm[i] = j;
                }
            }
        }
        return new Permutation(perm);
    }

    /**
     * Calculating the canonical permutation of a row.
     *
     * <p>In a block, the original and the other rows are compared; if there is a permutation
     * mapping rows to each other, canonical permutation, else id permutation is returned.
     *
     * @param partition int[] partition
     * @param max int[] max
     * @param check int[] check
     * @return int[]
     * @throws IOException
     */
    public static int[] getCanonicalPermutation2(int[] partition, int[] max, int[] check)
            throws IOException {
        int[] values = idValues(sum(partition));
        int i = 0;
        
        if (!equalSetCheck(max, check, partition)) {
            return values;
        } else {
            int limit = findZeros(partition);
            for (int s = 0; s < limit; s++) {
                int[] can = getBlocks(max, i, partition[s] + i);
                int[] non = getBlocks(check, i, partition[s] + i);
                values = getCyclesList(can, non, i, values);
                i = i + partition[s];
            }
            return values;
        }
    }

    public static int[] getCyclesList(int[] max, int[] non, int index, int[] values) {
        int i = 0;
        int permutationIndex = 0;
        while (i < max.length && max[i] != 0) {
            if (max[i] != non[i]) {
                permutationIndex = findMatch(max, non, max[i], i);
                if (i != permutationIndex) {
                    non = permuteArray(non, i, permutationIndex);
                }
                int temp = values[i + index];
                values[i + index] = values[permutationIndex + index];
                values[permutationIndex + index] = temp;
            }
            i++;
        }
        return values;
    }

    /**
     * @param max
     * @param non
     * @param value
     * @param start
     * @return
     */
    public static int findMatch(int[] max, int[] non, int value, int start) {
        int size = non.length;
        int index = start;
        for (int i = start; i < size; i++) {
            if (non[i] == value) {
                if (max[i] != non[i]) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public static Permutation getEqualPerm(
            Permutation cycleTransposition, int index, int[][] A, int[] newPartition)
            throws IOException {
        int[] check = row2compare(index, A, cycleTransposition);
        Permutation canonicalPermutation = getCanonicalPermutation(A[index], check, newPartition);
        return canonicalPermutation;
    }

    public static Permutation getCanonicalCycle(
            int index,
            int y,
            int total,
            int[][] A,
            int[] newPartition,
            Permutation cycleTransposition)
            throws IOException {
        biggest = true;
        Permutation canonicalPermutation = idPermutation(total);
        if (!equalRowsCheck(index, A, cycleTransposition, canonicalPermutation)) {
            canonicalPermutation = getEqualPerm(cycleTransposition, index, A, newPartition);
            int[] check = row2compare(index, A, cycleTransposition);
            check = actArray(check, canonicalPermutation);
        }
        return canonicalPermutation;
    }

    public static boolean check(int index, int y, int total, int[][] A, int[] newPartition)
            throws IOException {
        boolean check = true;
        ArrayList<Permutation> formerList = new ArrayList<>();
        ArrayList<Permutation> form = formerPermutations.get(index);
        for (Permutation permutation : form) {
            setBiggest(index, A, permutation, newPartition);
            if (biggest) {
                Permutation canonicalPermutation =
                        getCanonicalCycle(index, y, total, A, newPartition, permutation);
                int[] test = row2compare(index, A, permutation);
                test = actArray(test, canonicalPermutation);
                if (descendingOrderUpperMatrixCheck(index, newPartition, A[index], test)) {
                    if (canonicalPermutation.isIdentity()) {
                        if (equalSetCheck2(newPartition, A[index], test)) {
                            formerList.add(permutation);
                        }
                    } else {
                        Permutation newPermutation = canonicalPermutation.multiply(permutation);
                        formerList.add(newPermutation);
                    }
                } else {
                    formerList.clear();
                    check = false;
                    break;
                }
            } else {
                formerList.clear();
                check = false;
                break;
            }
        }
        if (check) {
            formerPermutations.get(index).clear();
            formerPermutations.set(index, formerList);
        }
        return check;
    }

    public static ArrayList<Permutation> cycleTranspositions(int index, int[] partition) {
        ArrayList<Permutation> perms = new ArrayList<Permutation>();
        int lValue = LValue(partition, index);
        int[] values;
        int former;
        for (int i = 0; i < lValue; i++) {
            values = idValues(size);
            former = values[index];
            values[index] = values[index + i];
            values[index + i] = former;
            Permutation p = new Permutation(values);
            perms.add(p);
        }
        return perms;
    }

    /**
     * Grund Thesis 3.3.3. To calculate the number of conjugacy classes, used in cycle transposition
     * calculation.
     *
     * @param partEx int[] former atom partition
     * @param degree the degree
     * @return
     */
    public static int LValue(int[] partEx, int degree) {
        return (sum(partEx, (degree)) - (degree));
    }

    /**
     * To get the canonical partition like in Grund Thesis 3.3.11
     *
     * @param i int row index
     * @param partition int[] partition
     * @return int[]
     * @throws IOException
     */
    public static int[] canonicalPartition(int i, int[] partition) throws IOException {
        return partitionCriteria(partition, i + 1);
    }

    /** Add number of 1s into an ArrayList */
    public static void addOnes(int[] list, int number) {
        for (int i = 0; i < number; i++) {
            list[i] = 1;
        }
    }

    public static int findZeros(int[] array) throws IOException {
        int index = size;
        for (int i = 0; i < size; i++) {
            if (array[i] == 0) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * Grund Thesis 3.3.2 Partitioning criteria (DONE)
     *
     * @param partEx the former partition
     * @param degree degree of the partitioning.
     * @return
     * @throws IOException
     */
    public static int[] partitionCriteria(int[] partEx, int degree) throws IOException {
        int[] partNew = new int[size];
        int limit = findZeros(partEx);
        if (zero(partEx)) {
            addOnes(partNew, degree);
            int index = degree;
            int oldValue = partEx[degree - 1];
            if (oldValue > 1) {
                partNew[index] = oldValue - 1;
                index++;
                for (int k = degree; k < limit; k++) {
                    partNew[index] = partEx[k];
                    index++;
                }
            } else if (oldValue == 1) {
                for (int k = degree; k < limit; k++) {
                    partNew[index] = partEx[k];
                    index++;
                }
            }
            return partNew;
        } else {
            return partEx;
        }
    }

    public static int[] orderDegreeSymbols(int[] degree, String[] symbol, int index0, int index1) {
        int temp = 0;
        for (int i = index0; i < index1; i++) {
            for (int j = i + 1; j < index1; j++) {
                if (degree[i] > degree[j]) {
                    swap(symbol, i, j);
                    temp = degree[i];
                    degree[i] = degree[j];
                    degree[j] = temp;
                }
            }
        }
        return degree;
    }

    public static void swap(String[] array, int i, int j) {
        String temp = "";
        temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static void swap(int[] array, int i, int j) {
        int swapString = array[i];
        array[i] = array[j];
        array[j] = swapString;
    }

    public static void sortWithPartition(int[] partitionList, int[] degrees, String[] symbols) {
        int[] partition = buildArray(partitionList);
        int size = partition.length;
        for (int n = 0; n < size; n++) {
            for (int m = 0; m < (size - 1) - n; m++) {
                if ((partition[m] > partition[m + 1])) {
                    swap(partition, m, (m + 1));
                    swap(degrees, m, (m + 1));
                    swap(symbols, m, (m + 1));
                }
            }
        }
        reOrder(partition, degrees, symbols);
        initialPartition(partition);
    }

    public static void initialPartition(int[] partition) {
        int index = 0;
        int index2 = 0;
        int part;
        int[] init = new int[size];
        while (index != hIndex) {
            part = partition[index];
            init[index2++] = part;
            index += part;
        }
        initialPartition = init;
    }

    public static int[] buildArray(int[] partition) {
        int[] partitionArray = new int[sum(partition)];
        int index = 0;
        for (int p : partition) {
            for (int i = 0; i < p; i++) {
                partitionArray[index] = p;
                index++;
            }
        }
        return partitionArray;
    }

    public static void reOrder(int[] partition, int[] degrees, String[] symbols) {
        int index = 0;
        int part;
        while (index != (hIndex)) {
            part = partition[index];
            orderDegreeSymbols(degrees, symbols, index, (index + part));
            index = index + part;
        }
    }

    private void parseArgs(String[] args) throws ParseException {
        Options options = setupOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            MAYGEN.formula = cmd.getOptionValue("formula");
            if (cmd.hasOption("filedir")) {
                MAYGEN.writeSDF = true;
                String filedir = cmd.getOptionValue("filedir");
                MAYGEN.filedir = Objects.isNull(filedir) ? "." : filedir;
            }
            if (cmd.hasOption("verbose")) MAYGEN.verbose = true;
            if (cmd.hasOption("tsvoutput")) MAYGEN.tsvoutput = true;
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setOptionComparator(null);
            String header =
                    "\nGenerates 	molecular structures for a given molecular formula."
                            + " The input is a molecular formula string."
                            + "For example 'C2OH4'."
                            + "If user wants an output file, the directory is needed to be specified."
                            + "file. \n\n";
            String footer = "\nPlease report issues at https://github.com/MehmetAzizYirik/MAYGEN";
            formatter.printHelp("java -jar MAYGEN.jar", header, options, footer, true);
            throw new ParseException("Problem parsing command line");
        }
    }

    private Options setupOptions() {
        Options options = new Options();
        Option formula =
                Option.builder("f")
                        .required(true)
                        .hasArg()
                        .longOpt("formula")
                        .desc("formula (required)")
                        .build();
        options.addOption(formula);
        Option verbose =
                Option.builder("v")
                        .required(false)
                        .longOpt("verbose")
                        .desc("print message")
                        .build();
        options.addOption(verbose);
        Option tvsoutput =
                Option.builder("t")
                        .required(false)
                        .longOpt("tsvoutput")
                        .desc(
                                "Output formula, number of structures and execution time in CSV format")
                        .build();
        options.addOption(tvsoutput);
        Option filedir =
                Option.builder("d")
                        .required(false)
                        .hasArg()
                        .optionalArg(true)
                        .longOpt("filedir")
                        .desc("Store output in given file")
                        .build();
        options.addOption(filedir);
        return options;
    }

    public static int numberOfBonds(int[][] mat) {
        int length = mat.length;
        //if(!justH) length =hIndex;
        int count = 0;
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                if (mat[i][j] != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public static String indexWithSpace(int i) {
    	String sourceDepiction="";
    	if(String.valueOf(i + 1).length()==1){
       	 sourceDepiction = "  "+String.valueOf(i + 1);
        }else if(String.valueOf(i + 1).length()==2){
       	 sourceDepiction = " "+String.valueOf(i + 1);
        }else{
       	 sourceDepiction = String.valueOf(i + 1);
        }
    	return sourceDepiction;
    }
    
    public static void write2SDF(int[] ar) throws IOException {
       int numberOfBonds= ar.length;
	   outFile.write("\nMolecule "+String.valueOf(count)+"\n    MAYGEN 20210615\n");
	    String allAtoms = "";
	    String allBonds = "";
	    if(String.valueOf(matrixSize).length()==1){
	        allAtoms+="  "+String.valueOf(matrixSize);
	    }else if (String.valueOf(matrixSize).length()==2){
	        allAtoms+=" "+String.valueOf(matrixSize);
	    }else{
	        allAtoms+=String.valueOf(matrixSize);
	    }

	    if(String.valueOf(numberOfBonds).length()==1){
	        allBonds+="  "+String.valueOf(numberOfBonds);
	    }else if (String.valueOf(numberOfBonds).length()==2){
	        allBonds+=" "+String.valueOf(numberOfBonds);
	    }else{
	        allBonds+=String.valueOf(numberOfBonds);
	    }



	    outFile.write(allAtoms+allBonds+"  0     0  0  0  0  0  0999 V2000\n");
 	   for (int i = 0; i < matrixSize/2; i++) {
 	      outFile.write("    0.0000    0.0000    0.0000 "+"O"+"   0  0  0  0  0  0  0  0  0  0  0  0\n");
 	   }
   	   
	   for (int i = 0; i < matrixSize/2; i++) {
 	      outFile.write("    0.0000    0.0000    0.0000 "+"S"+"   0  0  0  0  0  0  0  0  0  0  0  0\n");
 	   }
	   
   	   buildOnSm(ar);
   	   outFile.write("M  END\n");
   	   outFile.write("\n$$$$\n");
   	}
    
    public static void buildOnSm(int[] ar) throws IOException {
    	int oxygen=0;
    	int sulfur = graphSize/2;
    	int x=0;
    	int y=0;
    	
    	if(ar[graphSize-1]==0) {
    		x=sulfur-1;
    	}else {
    		x=graphSize-1;
    	}
    	
    	if(ar[0]==0) {
    		y=0;
    	}else {
    		y=sulfur;
    	}
    	String sourceDepiction=indexWithSpace(x);
    	String targetDepiction=indexWithSpace(y);
    	outFile.write(sourceDepiction+targetDepiction+"  "+String.valueOf(1)+"   0  0  0  0\n");
    	for(int i=0;i<graphSize-1;i++) {
    		if(ar[i]==0) {
    			x=oxygen;
    			oxygen++;
    		}else {
    			x=sulfur;
    			sulfur++;
    		}
    		if(ar[i+1]==0) {
    			y=oxygen;
    		}else {
    			y=sulfur;
    		}
    		sourceDepiction=indexWithSpace(x);
        	targetDepiction=indexWithSpace(y);
        	outFile.write(sourceDepiction+targetDepiction+"  "+String.valueOf(1)+"   0  0  0  0\n");
    	}
    }
    public static void write2SDF(int[][] mat) throws IOException {
    	   int numberOfBonds= numberOfBonds(mat);
    	   outFile.write("\nMolecule "+String.valueOf(count)+"\n    MAYGEN 20210615\n");
    	    String allAtoms = "";
    	    String allBonds = "";
    	    if(String.valueOf(matrixSize).length()==1){
    	        allAtoms+="  "+String.valueOf(matrixSize);
    	    }else if (String.valueOf(matrixSize).length()==2){
    	        allAtoms+=" "+String.valueOf(matrixSize);
    	    }else{
    	        allAtoms+=String.valueOf(matrixSize);
    	    }

    	    if(String.valueOf(numberOfBonds).length()==1){
    	        allBonds+="  "+String.valueOf(numberOfBonds);
    	    }else if (String.valueOf(numberOfBonds).length()==2){
    	        allBonds+=" "+String.valueOf(numberOfBonds);
    	    }else{
    	        allBonds+=String.valueOf(numberOfBonds);
    	    }



    	    outFile.write(allAtoms+allBonds+"  0     0  0  0  0  0  0999 V2000\n");
    	   for (int i = 0; i < matrixSize; i++) {
    	      outFile.write("    0.0000    0.0000    0.0000 "+symbolArray[i]+"   0  0  0  0  0  0  0  0  0  0  0  0\n");
    	   }

    	   for (int i = 0; i < matrixSize; i++) {
    	      for (int j = i + 1; j < matrixSize; j++) {
    	         if (mat[i][j]!=0) {
    	             String sourceDepiction = "";
    	             String targetDepiction = "";
    	             if(String.valueOf(i + 1).length()==1){
    	                    sourceDepiction = "  "+String.valueOf(i + 1);
    	                }else if(String.valueOf(i + 1).length()==2){
    	                    sourceDepiction = " "+String.valueOf(i + 1);
    	                }else{
    	                    sourceDepiction = String.valueOf(i + 1);
    	                }

    	                if(String.valueOf(j + 1).length()==1){
    	                    targetDepiction = "  "+String.valueOf(j + 1);
    	                }else if(String.valueOf(j + 1).length()==2){
    	                    targetDepiction = " "+String.valueOf(j + 1);
    	                }else{
    	                    targetDepiction = String.valueOf(j + 1);
    	                }

    	            outFile.write(sourceDepiction+targetDepiction+"  "+String.valueOf(mat[i][j])+"   0  0  0  0\n");
    	         }
    	      }
    	   }


    	    outFile.write("M  END\n");

    	    outFile.write("\n$$$$\n");

    	}

    public static void write2SDF(int[][] mat, String symbol) throws IOException {
    	int numberOfBonds= numberOfBonds(mat);
 	    outFile.write("\nMolecule "+String.valueOf(count)+"\n    MAYGEN 20210615\n");
 	    String allAtoms = "";
 	    String allBonds = "";
 	    if(String.valueOf(matrixSize).length()==1){
 	        allAtoms+="  "+String.valueOf(matrixSize);
 	    }else if (String.valueOf(matrixSize).length()==2){
 	        allAtoms+=" "+String.valueOf(matrixSize);
 	    }else{
 	        allAtoms+=String.valueOf(matrixSize);
 	    }

 	    if(String.valueOf(numberOfBonds).length()==1){
 	        allBonds+="  "+String.valueOf(numberOfBonds);
 	    }else if (String.valueOf(numberOfBonds).length()==2){
 	        allBonds+=" "+String.valueOf(numberOfBonds);
 	    }else{
 	        allBonds+=String.valueOf(numberOfBonds);
 	    }

 	    outFile.write(allAtoms+allBonds+"  0     0  0  0  0  0  0999 V2000\n");

 	   for (int i = 0; i < matrixSize; i++) {
 	      outFile.write("    0.0000    0.0000    0.0000 "+symbol+"   0  0  0  0  0  0  0  0  0  0  0  0\n");
 	   }

 	   for (int i = 0; i < matrixSize; i++) {
 	      for (int j = i + 1; j < matrixSize; j++) {
 	         if (mat[i][j]!=0) {
 	             String sourceDepiction = "";
 	             String targetDepiction = "";
 	             if(String.valueOf(i + 1).length()==1){
 	                    sourceDepiction = "  "+String.valueOf(i + 1);
 	                }else if(String.valueOf(i + 1).length()==2){
 	                    sourceDepiction = " "+String.valueOf(i + 1);
 	                }else{
 	                    sourceDepiction = String.valueOf(i + 1);
 	                }

 	                if(String.valueOf(j + 1).length()==1){
 	                    targetDepiction = "  "+String.valueOf(j + 1);
 	                }else if(String.valueOf(j + 1).length()==2){
 	                    targetDepiction = " "+String.valueOf(j + 1);
 	                }else{
 	                    targetDepiction = String.valueOf(j + 1);
 	                }

 	            outFile.write(sourceDepiction+targetDepiction+"  "+String.valueOf(mat[i][j])+"   0  0  0  0\n");
 	         }
 	      }
 	   }


 	    outFile.write("M  END\n");

 	    outFile.write("\n$$$$\n");

 	}
    
    public static void degree2graph() throws IOException {
        int[][] mat = new int[matrixSize][matrixSize];
        mat[0][1] = 1;
        mat[0][2] = 1;
        for (int i = 1; i < matrixSize - 2; i++) {
            mat[i][i + 2] = 1;
        }
        mat[matrixSize - 2][matrixSize - 1] = 1;
        count++;
        String symbol = "";
        if (oxygen == 0) {
            symbol="S";
        } else {
            symbol="O";
        }
        if (writeSDF) write2SDF(mat, symbol);
    }

    /**
     * The following functions are for the distribution of given number of sulfurs and oxygens in
     * regular graphs as node labelling. These functions are the Java implementation of Sawada's
     * method[1][2] in mathematical chemistry.
     *
     * <p>References:
     *
     * <p>[1] Sawada J. Generating bracelets in constant amortized time. SIAM Journal on Computing.
     * 2001;31(1):259-68. [2] http://www.cis.uoguelph.ca/~sawada/prog/necklaces.c
     */
    /**
     * The function to compare a node labelling array when the number of left consecutive entries is
     * equal to the right consecutive entries.
     *
     * <p>From this comparison, function returns :
     *
     * <p>1: if node labelling is same as its reversal.
     *
     * <p>0: if node labelling is smaller than its reversal.
     *
     * <p>-1: if node labelling is bigger than its reversal.
     *
     * <p>Reversal check helps to avoid duplicates, easy way of rotational symmetry check.
     *
     * @param length int node labelling length
     * @param index int starting index in the array
     * @return
     */
    public static int reverseComparison(int length, int index) {
        for (int i = index + 1; i <= (length + 1) / 2; i++) {
            if (nodeLabels[i] < nodeLabels[length - i + 1]) {
                return 0;
            } else if (nodeLabels[i] > nodeLabels[length - i + 1]) {
                return -1;
            }
        }
        return 1;
    }

    public static int[] build() throws IOException {
        int[] arr = new int[graphSize];
        for (int i = 1; i < graphSize+1; i++) {
            arr[i-1]=nodeLabels[i];
        }
        return arr;
    }
    

    /**
     * Main function for the distribution of atom symbols: O and S for OnSm form formulae.
     *
     * @param oxy int number of oxygens to distribute
     * @param sul int number of sulfur to distribute
     * @param nextSize int length of the next labelling
     * @param currentSize int length of the current labelling
     * @param reversedLength int longest node labelling, equal to its reversal
     * @param leftEquivalents int the number of consequtively equivalent values at the left side of
     *     the array
     * @param rightEquivalents int the number of consequtively equivalent values at the left side of
     *     the array
     * @param reversalIsSmaller boolean from the reversal comparison, using the boolean variable to
     *     know reveral is smaller than the bode labelling or not.
     * @throws IOException 
     */
    public static void distributeSymbols(
            int oxy,
            int sul,
            int nextSize,
            int currentSize,
            int reversedLength,
            int leftEquivalents,
            int rightEquivalents,
            boolean reversalIsSmaller) throws IOException {

        if (2 * (nextSize - 1) > (graphSize + reversedLength)) {
            if (nodeLabels[nextSize - 1] > nodeLabels[graphSize - nextSize + 2 + reversedLength])
                reversalIsSmaller = false;
            else if (nodeLabels[nextSize - 1]
                    < nodeLabels[graphSize - nextSize + 2 + reversedLength])
                reversalIsSmaller = true;
        }
        if (nextSize > graphSize) {
            if (!reversalIsSmaller && (graphSize % currentSize) == 0) {
                count++;
                if (writeSDF) write2SDF(build());
            }
        } else {
            int oxy2 = oxy;
            int sul2 = sul;

            nodeLabels[nextSize] = nodeLabels[nextSize - currentSize];

            if (nodeLabels[nextSize] == 0) {
                oxy2--;
            } else {
                sul2--;
            }
            if (nodeLabels[nextSize] == nodeLabels[1]) {
                rightEquivalents++;
            } else {
                rightEquivalents = 0;
            }
            if ((leftEquivalents == (nextSize - 1)) && (nodeLabels[nextSize - 1] == nodeLabels[1]))
                leftEquivalents++; // left consecutive element number incremention.

            if ((oxy2 >= 0)
                    && (sul2 >= 0)
                    && !((nextSize == graphSize)
                            && (leftEquivalents != graphSize)
                            && (nodeLabels[graphSize] == nodeLabels[1]))) {
                if (leftEquivalents == rightEquivalents) {
                    int reverse = reverseComparison(nextSize, leftEquivalents);
                    if (reverse == 0) {
                        distributeSymbols(
                                oxy2,
                                sul2,
                                nextSize + 1,
                                currentSize,
                                reversedLength,
                                leftEquivalents,
                                rightEquivalents,
                                reversalIsSmaller);

                    } else if (reverse == 1) {
                        distributeSymbols(
                                oxy2,
                                sul2,
                                nextSize + 1,
                                currentSize,
                                nextSize,
                                leftEquivalents,
                                rightEquivalents,
                                false);
                    }
                } else {
                    distributeSymbols(
                            oxy2,
                            sul2,
                            nextSize + 1,
                            currentSize,
                            reversedLength,
                            leftEquivalents,
                            rightEquivalents,
                            reversalIsSmaller);
                }
            }

            if (leftEquivalents == nextSize) {
                leftEquivalents--;
            }

            if (nodeLabels[nextSize - currentSize] == 0 && sul > 0) {
                nodeLabels[nextSize] = 1;

                if (nextSize == 1) {
                    distributeSymbols(
                            oxy, sul - 1, nextSize + 1, nextSize, 1, 1, 1, reversalIsSmaller);
                } else {
                    distributeSymbols(
                            oxy,
                            sul - 1,
                            nextSize + 1,
                            nextSize,
                            reversedLength,
                            leftEquivalents,
                            0,
                            reversalIsSmaller);
                }
            }
        }
    }

    public static void distributeSulfurOxygen() throws IOException {
        graphSize = oxygen + sulfur;
        nodeLabels = new int[graphSize + 1];
        nodeLabels[0] = 0;
        distributeSymbols(oxygen, sulfur, 1, 1, 0, 0, 0, false);
    }
    
    public static void main(String[] args)
            throws CloneNotSupportedException, CDKException, IOException {
    	MAYGEN gen = new MAYGEN();
        try {
            gen.parseArgs(args);
            MAYGEN.run();
        } catch (Exception e) {
            if (MAYGEN.verbose) e.getCause();
        }
    }
}
