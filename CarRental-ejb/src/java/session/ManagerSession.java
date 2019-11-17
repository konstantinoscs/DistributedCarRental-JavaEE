package session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.CompanyLoader;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    private static int nextuid = 0;
    
    @PersistenceContext(unitName = "CarRental-ejbPU")
    EntityManager em;


    
    @Override
    public Set<CarType> getCarTypes(String company) {
        TypedQuery<CarRentalCompany> q = em.createNamedQuery("getCompanyByName", CarRentalCompany.class)
                .setParameter("name", company);
        CarRentalCompany crc = q.getSingleResult();
        try {
            return new HashSet<CarType>(crc.getAllTypes());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        TypedQuery<CarRentalCompany> q = em.createNamedQuery("getCompanyByName", CarRentalCompany.class)
                .setParameter("name", company);
        CarRentalCompany crc = q.getSingleResult();
        try {
            for(Car c: crc.getCars(type)){
                out.add(c.getId());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        TypedQuery<CarRentalCompany> q = em.createNamedQuery("getCompanyByName", CarRentalCompany.class)
                .setParameter("name", company);
        CarRentalCompany crc = q.getSingleResult();
        try {
            return crc.getCar(id).getReservations().size();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        Set<Reservation> out = new HashSet<Reservation>();
        TypedQuery<CarRentalCompany> q = em.createNamedQuery("getCompanyByName", CarRentalCompany.class)
                .setParameter("name", company);
        CarRentalCompany crc = q.getSingleResult();
        try {
            for(Car c: crc.getCars(type)){
                out.addAll(c.getReservations());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return out.size();
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