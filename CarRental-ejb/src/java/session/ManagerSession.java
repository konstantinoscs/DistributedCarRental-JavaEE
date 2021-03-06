package session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    private static int nextuid = 0;
    
    @PersistenceContext(unitName = "CarRental-ejbPU")
    EntityManager em;
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        TypedQuery<CarType> q = em.createNamedQuery("getAllCarTypes", CarType.class)
                .setParameter("name", company);
        List<CarType> carTypes = q.getResultList();
        try {
            return new HashSet<CarType>(carTypes);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = null;
        TypedQuery<Integer> q = em.createNamedQuery("getCarIds", Integer.class)
                .setParameter("name", company)
                .setParameter("type", type);
        try {
            out = new HashSet<Integer>(q.getResultList());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    //get number of reservations of a specific car
    @Override
    public int getNumberOfReservations(String company, String type, int id) throws Exception {
        TypedQuery<Long> q = em.createNamedQuery("getReservationsOfCar", Long.class)
                .setParameter("id", id);
            return q.getSingleResult().intValue();
    }

    //get number of reservations for a carType
    @Override
    public int getNumberOfReservations(String company, String CarType) throws Exception {
        TypedQuery<Long> q = em.createNamedQuery("getReservationsOfCarTypeInCompany", Long.class)
                .setParameter("company", company)
                .setParameter("type", CarType);
        return q.getSingleResult().intValue();
    }
    
    @Override
    public int getNumberOfReservationsBy(String clientName) {
        int noOfReservations = 0;
        TypedQuery<CarRentalCompany> q = em.createNamedQuery("getAllRentalCompanies", CarRentalCompany.class);
        List<CarRentalCompany> companies = q.getResultList();
        for (CarRentalCompany company : companies) {
            noOfReservations += company.getNoOfReservationsBy(clientName);
        }
        return noOfReservations;
    }
    
    @Override
    public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year) throws Exception {
        TypedQuery<String> q = em.createNamedQuery("getAllRentalCompaniesNames", String.class);
        Set<String> companies =  new HashSet<String>(q.getResultList());
        if (!companies.contains(carRentalCompanyName))
            throw new Exception("Requested Car Rental Company is not registered!");
        CarRentalCompany crc = em.find(CarRentalCompany.class, carRentalCompanyName);
        return crc.getMostPopularCarTypeIn(year);
    }
    
    public Set<String> getBestClients() {
        Map<String, Integer> reservations = new HashMap<>();
        TypedQuery<CarRentalCompany> q = em.createNamedQuery("getAllRentalCompanies", CarRentalCompany.class);
        List<CarRentalCompany> companies = q.getResultList();
        for (CarRentalCompany company : companies) {
            updateMap(reservations, company.getClientsWithReservations());
        }
        
        //max = Collections.max(reservations.entrySet(), Map.Entry.comparingByValue()).getValue();
        Map.Entry<String, Integer> maxEntry = null;
        for (Map.Entry<String, Integer> entry : reservations.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        // after we found the max, get all clients that have max number of reservations
        Set<String> best = new HashSet<>();
        for (Map.Entry<String, Integer> entry : reservations.entrySet()) {
            if(entry.getValue().equals(maxEntry.getValue()))
                best.add(entry.getKey());
        }
        return best;
    }
    
    
    private void updateMap(Map<String, Integer> reservations, Map<String, Integer> tempReservations) {
        for (Map.Entry<String, Integer> entry : tempReservations.entrySet()) {
            reservations.put(entry.getKey(), reservations.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
    }
    

    @Override
    public void loadCarRentalCompany(String file) {
        CarRentalCompany company = null;
        try {
            CrcData data = loadData(file);
            company = new CarRentalCompany(data.name, data.regions, data.cars);
            Logger.getLogger(ManagerSession.class.getName()).log(Level.INFO, "Loaded {0} from file {1}", new Object[]{data.name, file});
        } catch (NumberFormatException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
        }
        em.persist(company);
    }
    
    private static CrcData loadData(String datafile)
            throws NumberFormatException, IOException {

        CrcData out = new CrcData();
        StringTokenizer csvReader;
       
        //open file from jar
        BufferedReader in = new BufferedReader(new InputStreamReader(ManagerSession.class.getClassLoader().getResourceAsStream(datafile)));
        
        try {
            while (in.ready()) {
                String line = in.readLine();
                
                if (line.startsWith("#")) {
                    // comment -> skip					
                } else if (line.startsWith("-")) {
                    csvReader = new StringTokenizer(line.substring(1), ",");
                    out.name = csvReader.nextToken();
                    out.regions = Arrays.asList(csvReader.nextToken().split(":"));
                } else {
                    csvReader = new StringTokenizer(line, ",");
                    //create new car type from first 5 fields
                    CarType type = new CarType(csvReader.nextToken(),
                            Integer.parseInt(csvReader.nextToken()),
                            Float.parseFloat(csvReader.nextToken()),
                            Double.parseDouble(csvReader.nextToken()),
                            Boolean.parseBoolean(csvReader.nextToken()));
                    //create N new cars with given type, where N is the 5th field
                    for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
                        nextuid++;
                        out.cars.add(new Car(nextuid, type));
                    }        
                }
            } 
        } finally {
            in.close();
        }

        return out;
    }
    
    static class CrcData {
            public List<Car> cars = new LinkedList<Car>();
            public String name;
            public List<String> regions =  new LinkedList<String>();
    }

}