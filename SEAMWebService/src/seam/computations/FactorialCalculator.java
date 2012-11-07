/**
 * 
 */
package seam.computations;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Pralav
 *
 */
public class FactorialCalculator extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void service(HttpServletRequest request, HttpServletResponse response){
		try {
			PrintWriter out = response.getWriter();
			
			String inputNumber = request.getParameter("number");
			
			if(inputNumber == null){
				out.write("FAILURE|Please enter a valid number");
			}else{

				int inputInteger = Integer.valueOf(inputNumber).intValue();
				
				int factorial = 1;
				
				while(inputInteger != 0){
					factorial = factorial * inputInteger--;
				}
				
				out.write("SUCCESS|"+factorial);
				
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
