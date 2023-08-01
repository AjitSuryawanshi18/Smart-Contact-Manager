import java.util.Random;

public class otp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Random random = new Random();
		int otp = 100000 + random.nextInt(900000);
		System.out.println(otp);
	}

}
