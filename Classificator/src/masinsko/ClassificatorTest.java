package masinsko;

import java.util.ArrayList;
import java.util.HashMap;

/*
 *  Implemented by Zafir Stojanovski (151015) & Branko Furnadziski (151025), 11.11.2017.
 */

public class ClassificatorTest {
	
	public static void main(String[] args) {
		Classificator classificator = new Classificator();
		
		System.out.println(classificator.classify(4.7, Classificator.GIMNAZISKO, 9, Classificator.EDEN_DO_DVA));
		System.out.println(classificator.classify(3, Classificator.GIMNAZISKO, 9, Classificator.EDEN_DO_DVA));
		System.out.println(classificator.classify(3, Classificator.SREDNO_STRUCNO, 8, Classificator.POVEKE_OD_PET));	
	}

}

class Classificator {
	public static final String GIMNAZISKO = "gimnazisko";
	public static final String SREDNO_STRUCNO = "sredno strucno";
	public static final String NA_VREME = "na vreme";
	public static final String SO_ZADOCNUVANJE = "so zadocnuvanje";
	public static final String NE_DIPLOMIRAL = "ne diplomiral";
	public static final String NULA = "0";
	public static final String EDEN_DO_DVA = "od 1-2";
	public static final String TRI_DO_PET = "od 3-5";
	public static final String POVEKE_OD_PET = "poveke od 5";
	public static final String PROSEK_SREDNO = "prosek sredno";
	public static final String PROSEK_PRVA = "prosek prva";
	public static final String MU = "mu";
	public static final String VARIANCE = "variance";
	
	
	private Parametars onTimeHighSchoolGpaParametars;
	private Parametars onTimeCollegeGpaParametars;
	private Parametars delayedHighSchoolGpaParametars;
	private Parametars delayedCollegeGpaParametars;
	private Parametars didntGraduateHighSchoolGpaParametars;
	private Parametars didntGraduateCollegeGpaParametars;
	
	private HighSchool onTimeHighSchool;
	private HighSchool delayedHighSchool;
	private HighSchool didntGraduateHighSchool;
	
	private FailedExams onTimeFailed;
	private FailedExams delayedFailed;
	private FailedExams didntGraduateFailed;
	
	private ArrayList<Tuple> tuples;
	private HashMap<String, Double> values;
	
	private long countOnTime;
	private long countDelayed;
	private long countDidntGraduate;
	private long total;
	
	public Classificator(){
		tuples = new ArrayList<>();
		values = new HashMap<>();
		initializeTable();
		initializeCounts();
		initializeContinuousParametars();
		initializeDiscreteParametars();
		fillMap();
	}

	private double gaussian(double x, double mu, double variance){
		return 1.0/(Math.sqrt(2*Math.PI*variance)) * Math.pow(Math.E, -Math.pow(x-mu,2)/(2*variance));
	}
	
	public String classify(double highSchoolGpa, String highSchool, double collegeGpa, String failedExams) {
		/*
		System.out.println("onTimeHighSchoolGpaParametars: " + onTimeHighSchoolGpaParametars.getMu() + " " +onTimeHighSchoolGpaParametars.getVariance());
		System.out.println("onTimeCollegeGpaParametars: " + onTimeCollegeGpaParametars.getMu() + " " +onTimeCollegeGpaParametars.getVariance());
		System.out.println("onTimeHighSchool: " + onTimeHighSchool.getGymnasium() + " " +onTimeHighSchool.getCraft());
		System.out.println("onTimeFailed: " + onTimeFailed.getZero() + " " +onTimeFailed.getOne_to_two() + " " + onTimeFailed.getThree_to_five() + " " + onTimeFailed.getMore_than_five());
		System.out.println();
		System.out.println("delayedHighSchoolGpaParametars: " + delayedHighSchoolGpaParametars.getMu() + " " +delayedHighSchoolGpaParametars.getVariance());
		System.out.println("delayedCollegeGpaParametars: " + delayedCollegeGpaParametars.getMu() + " " +delayedCollegeGpaParametars.getVariance());
		System.out.println("delayedHighSchool: " + delayedHighSchool.getGymnasium() + " " +delayedHighSchool.getCraft());
		System.out.println("delayedFailed: " + delayedFailed.getZero() + " " +delayedFailed.getOne_to_two() + " " + delayedFailed.getThree_to_five() + " " + delayedFailed.getMore_than_five());
		System.out.println();
		System.out.println("didntGraduateHighSchoolGpaParametars: " + didntGraduateHighSchoolGpaParametars.getMu() + " " +didntGraduateHighSchoolGpaParametars.getVariance());
		System.out.println("didntGraduateCollegeGpaParametars: " + didntGraduateCollegeGpaParametars.getMu() + " " +didntGraduateCollegeGpaParametars.getVariance());
		System.out.println("didntGraduateHighSchool: " + didntGraduateHighSchool.getGymnasium() + " " +didntGraduateHighSchool.getCraft());
		System.out.println("didntGraduateFailed: " + didntGraduateFailed.getZero() + " " +didntGraduateFailed.getOne_to_two() + " " + didntGraduateFailed.getThree_to_five() + " " + didntGraduateFailed.getMore_than_five());
		System.out.println();
		*/
		
		
		for (String name: values.keySet()){

            String key = name.toString();
            String value = values.get(name).toString();  
            System.out.println(key + " " + value);  

		} 
		
		
		double onTime = gaussian(highSchoolGpa, values.get(NA_VREME + PROSEK_SREDNO + MU), values.get(NA_VREME + PROSEK_SREDNO + VARIANCE))
				* values.get(NA_VREME + highSchool)
				* gaussian(collegeGpa, values.get(NA_VREME + PROSEK_PRVA + MU), values.get(NA_VREME + PROSEK_PRVA + VARIANCE))
				* values.get(NA_VREME + failedExams)
				* (1.0 * countOnTime / total);
		
		double delayed = gaussian(highSchoolGpa, values.get(SO_ZADOCNUVANJE + PROSEK_SREDNO + MU), values.get(SO_ZADOCNUVANJE + PROSEK_SREDNO + VARIANCE))
				* values.get(SO_ZADOCNUVANJE + highSchool)
				* gaussian(collegeGpa, values.get(SO_ZADOCNUVANJE + PROSEK_PRVA + MU), values.get(SO_ZADOCNUVANJE + PROSEK_PRVA + VARIANCE))
				* values.get(SO_ZADOCNUVANJE + failedExams)
				* (1.0 * countDelayed / total);
		
		double didntGraduate = gaussian(highSchoolGpa, values.get(NE_DIPLOMIRAL + PROSEK_SREDNO + MU), values.get(NE_DIPLOMIRAL + PROSEK_SREDNO + VARIANCE))
				* values.get(NE_DIPLOMIRAL + highSchool)
				* gaussian(collegeGpa, values.get(NE_DIPLOMIRAL + PROSEK_PRVA + MU), values.get(NE_DIPLOMIRAL + PROSEK_PRVA + VARIANCE))
				* values.get(NE_DIPLOMIRAL + failedExams)
				* (1.0 * countDidntGraduate / total);
		
		double max = Math.max(Math.max(onTime, delayed), didntGraduate);
		
		if (max == onTime) return NA_VREME;
		else if (max == delayed) return SO_ZADOCNUVANJE;
		return NE_DIPLOMIRAL;
	}
	
	private Parametars calculateHighSchoolGpaParametars(String category){
		double mu = tuples.stream().filter(i -> i.getGraduated().equals(category)).mapToDouble(i -> i.getHighSchoolGpa()).average().orElse(0);
		double variance = tuples.stream().filter(i -> i.getGraduated().equals(category)).mapToDouble(i -> Math.pow(i.getHighSchoolGpa() - mu, 2)).average().orElse(0);
		
		return new Parametars(mu, variance);
	}
	
	private Parametars calculateCollegeGpaParametars(String category){
		double mu = tuples.stream().filter(i -> i.getGraduated().equals(category)).mapToDouble(i -> i.getCollegeGpa()).average().orElse(0);
		double variance = tuples.stream().filter(i -> i.getGraduated().equals(category)).mapToDouble(i -> Math.pow(i.getCollegeGpa() - mu, 2)).average().orElse(0);
		
		return new Parametars(mu, variance);
	}
	
	private HighSchool calculateHighSchool(String category, long total) {
		double gymnasium = 1.0*tuples.stream().filter(i -> i.getGraduated().equals(category)).filter(i -> i.getHighSchool().equals(GIMNAZISKO)).count();
		double craft = 1.0*tuples.stream().filter(i -> i.getGraduated().equals(category)).filter(i -> i.getHighSchool().equals(SREDNO_STRUCNO)).count();
		
		if (gymnasium == 0 || craft == 0){
			gymnasium = (gymnasium + 0.5)/(total+1);
			craft = (craft + 0.5)/(total+1);
		} else {
			gymnasium /= total;
			craft /= total;
		}
		
		return new HighSchool(gymnasium, craft);
	}
	
	private FailedExams calculateFailedExams(String category, long total) {
		double zero = 1.0*tuples.stream().filter(i -> i.getGraduated().equals(category)).filter(i -> i.getFailedExams().equals(NULA)).count();
		double one_to_two = 1.0*tuples.stream().filter(i -> i.getGraduated().equals(category)).filter(i -> i.getFailedExams().equals(EDEN_DO_DVA)).count();
		double three_to_five = 1.0*tuples.stream().filter(i -> i.getGraduated().equals(category)).filter(i -> i.getFailedExams().equals(TRI_DO_PET)).count();
		double more_than_five = 1.0*tuples.stream().filter(i -> i.getGraduated().equals(category)).filter(i -> i.getFailedExams().equals(POVEKE_OD_PET)).count();
		
		if (zero == 0 || one_to_two == 0 || three_to_five == 0 || more_than_five == 0){
			zero = (zero + 0.25)/(total + 1);
			one_to_two = (one_to_two + 0.25)/(total + 1);
			three_to_five = (three_to_five + 0.25)/(total + 1);
			more_than_five = (more_than_five + 0.25)/(total + 1);
		} else {
			zero /= total;
			one_to_two /= total;
			three_to_five /= total;
			more_than_five /= total;
		}
		
		return new FailedExams(zero, one_to_two, three_to_five, more_than_five);
	}
	
	private void initializeTable(){
		Tuple tuple = new Tuple(NA_VREME, 4.5, GIMNAZISKO, 8.31, EDEN_DO_DVA);
		tuples.add(tuple);
		
		tuple = new Tuple(NA_VREME, 5, GIMNAZISKO, 10, NULA);
		tuples.add(tuple);
		
		tuple = new Tuple(NA_VREME, 5, SREDNO_STRUCNO, 7.98, NULA);
		tuples.add(tuple);
		
		tuple = new Tuple(NA_VREME, 4.98, GIMNAZISKO, 9.42, NULA);
		tuples.add(tuple);
		
		tuple = new Tuple(NA_VREME, 4, GIMNAZISKO, 8.98, EDEN_DO_DVA);
		tuples.add(tuple);
		
		tuple = new Tuple(SO_ZADOCNUVANJE, 4.23, GIMNAZISKO, 8.23, TRI_DO_PET);
		tuples.add(tuple);
		
		tuple = new Tuple(SO_ZADOCNUVANJE, 3, SREDNO_STRUCNO, 8.56, EDEN_DO_DVA);
		tuples.add(tuple);
		
		tuple = new Tuple(SO_ZADOCNUVANJE, 5, GIMNAZISKO, 10, TRI_DO_PET);
		tuples.add(tuple);
		
		tuple = new Tuple(SO_ZADOCNUVANJE, 4.87, GIMNAZISKO, 7.99, POVEKE_OD_PET);
		tuples.add(tuple);
		
		tuple = new Tuple(SO_ZADOCNUVANJE, 4.56, SREDNO_STRUCNO, 8.98, TRI_DO_PET);
		tuples.add(tuple);
		
		tuple = new Tuple(NE_DIPLOMIRAL, 3.92, SREDNO_STRUCNO, 7, TRI_DO_PET);
		tuples.add(tuple);
		
		tuple = new Tuple(NE_DIPLOMIRAL, 3.45, SREDNO_STRUCNO, 5, POVEKE_OD_PET);
		tuples.add(tuple);
		
		tuple = new Tuple(NE_DIPLOMIRAL, 2.67, SREDNO_STRUCNO, 6.3, TRI_DO_PET);
		tuples.add(tuple);
		
		tuple = new Tuple(NE_DIPLOMIRAL, 3.2, SREDNO_STRUCNO, 8.2, POVEKE_OD_PET);
		tuples.add(tuple);
		
		tuple = new Tuple(NE_DIPLOMIRAL, 2, GIMNAZISKO, 6.2, POVEKE_OD_PET);
		tuples.add(tuple);
	}
	
	private void initializeCounts() {
		countOnTime = tuples.stream().filter(i -> i.getGraduated().equals(NA_VREME)).count();
		countDelayed = tuples.stream().filter(i -> i.getGraduated().equals(SO_ZADOCNUVANJE)).count();
		countDidntGraduate = tuples.stream().filter(i -> i.getGraduated().equals(NE_DIPLOMIRAL)).count();
		total = tuples.size();
	}
	
	private void initializeContinuousParametars() {
		onTimeHighSchoolGpaParametars = calculateHighSchoolGpaParametars(NA_VREME);
		onTimeCollegeGpaParametars = calculateCollegeGpaParametars(NA_VREME);
		delayedHighSchoolGpaParametars = calculateHighSchoolGpaParametars(SO_ZADOCNUVANJE);
		delayedCollegeGpaParametars = calculateCollegeGpaParametars(SO_ZADOCNUVANJE);
		didntGraduateHighSchoolGpaParametars = calculateHighSchoolGpaParametars(NE_DIPLOMIRAL);
		didntGraduateCollegeGpaParametars = calculateCollegeGpaParametars(NE_DIPLOMIRAL);
	}
	
	private void initializeDiscreteParametars() {
		onTimeHighSchool = calculateHighSchool(NA_VREME, countOnTime);
		delayedHighSchool = calculateHighSchool(SO_ZADOCNUVANJE, countDelayed);
		didntGraduateHighSchool = calculateHighSchool(NE_DIPLOMIRAL, countDidntGraduate);
		
		onTimeFailed = calculateFailedExams(NA_VREME, countOnTime);
		delayedFailed = calculateFailedExams(SO_ZADOCNUVANJE, countDelayed);
		didntGraduateFailed = calculateFailedExams(NE_DIPLOMIRAL, countDidntGraduate);
	}
	
	private void fillMap(){
		values.put(NA_VREME + PROSEK_SREDNO + MU, onTimeHighSchoolGpaParametars.getMu());
		values.put(NA_VREME + PROSEK_SREDNO + VARIANCE, onTimeHighSchoolGpaParametars.getVariance());
		values.put(NA_VREME + PROSEK_PRVA + MU, onTimeCollegeGpaParametars.getMu());
		values.put(NA_VREME + PROSEK_PRVA + VARIANCE, onTimeCollegeGpaParametars.getVariance());
		values.put(SO_ZADOCNUVANJE + PROSEK_SREDNO + MU, delayedHighSchoolGpaParametars.getMu());
		values.put(SO_ZADOCNUVANJE + PROSEK_SREDNO + VARIANCE, delayedHighSchoolGpaParametars.getVariance());
		values.put(SO_ZADOCNUVANJE + PROSEK_PRVA + MU, delayedCollegeGpaParametars.getMu());
		values.put(SO_ZADOCNUVANJE + PROSEK_PRVA + VARIANCE, delayedCollegeGpaParametars.getVariance());
		values.put(NE_DIPLOMIRAL + PROSEK_SREDNO + MU, didntGraduateHighSchoolGpaParametars.getMu());
		values.put(NE_DIPLOMIRAL + PROSEK_SREDNO + VARIANCE, didntGraduateHighSchoolGpaParametars.getVariance());
		values.put(NE_DIPLOMIRAL + PROSEK_PRVA + MU, didntGraduateCollegeGpaParametars.getMu());
		values.put(NE_DIPLOMIRAL + PROSEK_PRVA + VARIANCE, didntGraduateCollegeGpaParametars.getVariance());
		
		values.put(NA_VREME + GIMNAZISKO, onTimeHighSchool.getGymnasium());
		values.put(NA_VREME + SREDNO_STRUCNO, onTimeHighSchool.getCraft());
		values.put(SO_ZADOCNUVANJE + GIMNAZISKO, delayedHighSchool.getGymnasium());
		values.put(SO_ZADOCNUVANJE + SREDNO_STRUCNO, delayedHighSchool.getCraft());
		values.put(NE_DIPLOMIRAL + GIMNAZISKO, didntGraduateHighSchool.getGymnasium());
		values.put(NE_DIPLOMIRAL + SREDNO_STRUCNO, didntGraduateHighSchool.getCraft());
		
		values.put(NA_VREME + NULA, onTimeFailed.getZero());
		values.put(NA_VREME + EDEN_DO_DVA, onTimeFailed.getOne_to_two());
		values.put(NA_VREME + TRI_DO_PET, onTimeFailed.getThree_to_five());
		values.put(NA_VREME + POVEKE_OD_PET, onTimeFailed.getMore_than_five());
		values.put(SO_ZADOCNUVANJE + NULA, delayedFailed.getZero());
		values.put(SO_ZADOCNUVANJE + EDEN_DO_DVA, delayedFailed.getOne_to_two());
		values.put(SO_ZADOCNUVANJE + TRI_DO_PET, delayedFailed.getThree_to_five());
		values.put(SO_ZADOCNUVANJE + POVEKE_OD_PET, delayedFailed.getMore_than_five());
		values.put(NE_DIPLOMIRAL + NULA, didntGraduateFailed.getZero());
		values.put(NE_DIPLOMIRAL + EDEN_DO_DVA, didntGraduateFailed.getOne_to_two());
		values.put(NE_DIPLOMIRAL + TRI_DO_PET, didntGraduateFailed.getThree_to_five());
		values.put(NE_DIPLOMIRAL + POVEKE_OD_PET, didntGraduateFailed.getMore_than_five());
	}
	
}

class Tuple {
	private String graduated;
	private double highSchoolGpa;
	private String highSchool;
	private double collegeGpa;
	private String failedExams;
	
	public Tuple(String graduated, double highSchoolGpa, String highSchool, double collegeGpa, String failedExams) {
		this.graduated = graduated;
		this.highSchoolGpa = highSchoolGpa;
		this.highSchool = highSchool;
		this.collegeGpa = collegeGpa;
		this.failedExams = failedExams;
	}
	
	public String getGraduated() {
		return graduated;
	}

	public void setGraduated(String graduated) {
		this.graduated = graduated;
	}

	public double getHighSchoolGpa() {
		return highSchoolGpa;
	}

	public void setHighSchoolGpa(double highSchoolGpa) {
		this.highSchoolGpa = highSchoolGpa;
	}

	public String getHighSchool() {
		return highSchool;
	}

	public void setHighSchool(String highSchool) {
		this.highSchool = highSchool;
	}

	public double getCollegeGpa() {
		return collegeGpa;
	}

	public void setCollegeGpa(double collegeGpa) {
		this.collegeGpa = collegeGpa;
	}

	public String getFailedExams() {
		return failedExams;
	}

	public void setFailedExams(String failedExams) {
		this.failedExams = failedExams;
	}

	@Override
	public String toString() {
		return String.format("%s %.2f %s %.2f %s", graduated, highSchoolGpa, highSchool, collegeGpa, failedExams);
	}
	
}

class Parametars {
	private double mu;
	private double variance;
	
	public Parametars(double mu, double variance) {
		this.mu = mu;
		this.variance = variance;
	}

	public double getMu() {
		return mu;
	}

	public void setMu(double mu) {
		this.mu = mu;
	}

	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}
	
}

class HighSchool {
	private double gymnasium;
	private double craft;
	
	public HighSchool(double gymnasium, double craft) {
		this.gymnasium = gymnasium;
		this.craft = craft;
	}

	public double getGymnasium() {
		return gymnasium;
	}

	public void setGymnasium(double gymnasium) {
		this.gymnasium = gymnasium;
	}

	public double getCraft() {
		return craft;
	}

	public void setCraft(double craft) {
		this.craft = craft;
	}
}

class FailedExams{
	private double zero;
	private double one_to_two;
	private double three_to_five;
	private double more_than_five;
	
	public FailedExams(double zero, double one_to_two, double three_to_five, double more_than_five) {
		this.zero = zero;
		this.one_to_two = one_to_two;
		this.three_to_five = three_to_five;
		this.more_than_five = more_than_five;
	}

	public double getZero() {
		return zero;
	}

	public void setZero(double zero) {
		this.zero = zero;
	}

	public double getOne_to_two() {
		return one_to_two;
	}

	public void setOne_to_two(double one_to_two) {
		this.one_to_two = one_to_two;
	}

	public double getThree_to_five() {
		return three_to_five;
	}

	public void setThree_to_five(double three_to_five) {
		this.three_to_five = three_to_five;
	}

	public double getMore_than_five() {
		return more_than_five;
	}

	public void setMore_than_five(double more_than_five) {
		this.more_than_five = more_than_five;
	}
}