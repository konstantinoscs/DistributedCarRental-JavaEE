package session;

import java.util.Set;
import javax.ejb.Remote;
import rental.CarType;

@Remote
public interface ManagerSessionRemote {
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCarIds(String company,String type);
    
    public int getNumberOfReservations(String company, String type, int carId);
    
    public int getNumberOfReservations(String company, String type);
    
    public int getNumberOfReservationsBy(String clientName);
    
    public int getNumberOfReservationsForCarType(String carRentalName, String carType) throws Exception;
    
    public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year) throws Exception;
    
    public Set<String> getBestClients();
    
    public void loadCarRentalCompany(String file);
      
}