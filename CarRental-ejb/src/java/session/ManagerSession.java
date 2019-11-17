package session;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.CompanyLoader;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext(unitName = "CarRental-ejbPU")
    EntityManager em;


    
    @Override
    public Set<CarType> getCarTypes(String company) {
        Set<CarType> out = new HashSet<CarType>();
        try {
            //return new HashSet<CarType>(CompanyLoader.getRental(company).getAllTypes());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        try {
            //for(Car c: CompanyLoader.getRental(company).getCars(type)){
             //   out.add(c.getId());
           // }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        try {
           // return CompanyLoader.getRental(company).getCar(id).getReservations().size();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return 0;
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        Set<Reservation> out = new HashSet<Reservation>();
        try {
           // for(Car c: CompanyLoader.getRental(company).getCars(type)){
             //   out.addAll(c.getReservations());
           // }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return out.size();
    }

    @Override
    public void loadCarRentalCompany(String file) {
        CarRentalCompany company = CompanyLoader.loadRental(file);
        em.persist(company);
    }

}