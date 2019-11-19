package session;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
@TransactionManagement(TransactionManagementType.BEAN)
public class CarRentalSession implements CarRentalSessionRemote {
    
    @PersistenceContext
    EntityManager em;
    
    @Resource
    UserTransaction utx;
    
    private String renter;
    private List<Quote> quotes = new LinkedList<>();

    @Override
    public Set<String> getAllRentalCompanies() {
        TypedQuery<String> q = em.createNamedQuery("getAllRentalCompaniesNames", String.class);
        return new HashSet<>(q.getResultList());
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        List<CarType> availableCarTypes = new LinkedList<>();
        TypedQuery<CarRentalCompany> q = em.createNamedQuery("getAllRentalCompanies", CarRentalCompany.class);
        List<CarRentalCompany> companies = q.getResultList();
        for(CarRentalCompany crc : companies) {
            for(CarType ct : crc.getAvailableCarTypes(start, end)) {
                if(!availableCarTypes.contains(ct))
                    availableCarTypes.add(ct);
            }
        }
        return availableCarTypes;
    }
    
    @Override
    public String getCheapestCarType(Date start, Date end, String region) {
        double minPrice = Double.MAX_VALUE;
        String cheapestCarType = "";
        TypedQuery<CarRentalCompany> q = em.createNamedQuery("getAllRentalCompanies", CarRentalCompany.class);
        List<CarRentalCompany> companies = q.getResultList();
        for (CarRentalCompany company : companies) {
            if (!company.operatesInRegion(region)) {
                continue;
            }
            Set<CarType> carTypes = company.getAvailableCarTypes(start, end);
            for (CarType carType : carTypes) {
                if (carType.getRentalPricePerDay() < minPrice) {
                    minPrice = carType.getRentalPricePerDay();
                    cheapestCarType = carType.getName();
                }
            }

        }
        return cheapestCarType;
    }

    @Override
    public Quote createQuote(String clientName, ReservationConstraints constraints) throws ReservationException {
        TypedQuery<CarRentalCompany> q = em.createNamedQuery("getAllRentalCompanies", CarRentalCompany.class);
        List<CarRentalCompany> companies;
        try {   // if no companies found, throw excpetion
            companies = q.getResultList();
        } catch (Exception e) {
            throw new ReservationException(e);
        }
        
        Quote quote = null;

        for (CarRentalCompany company : companies) {
            try {
                quote = company.createQuote(constraints, clientName);
            } catch (Exception e) {
                continue;
            }
            break;
        }
        if (quote == null)
            throw new ReservationException("Didn't find an available quote for these constraints");
        
        this.quotes.add(quote);
        return quote;
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }
    

    @Override
    public List<Reservation> confirmQuotes() throws Exception {
        List<Reservation> done = new LinkedList<Reservation>();
        boolean failed = false;
        try {
            utx.begin();
            for (Quote quote : quotes) {
                CarRentalCompany company = em.find(CarRentalCompany.class, quote.getRentalCompany());
                Reservation res = company.confirmQuote(quote);
                done.add(res);
                em.persist(res);
            }
            utx.commit();
        } catch (Exception e) {
            failed = true;
            if (utx.getStatus()==Status.STATUS_ACTIVE){
                utx.rollback();
            }
        }
        
        if (failed) {
            throw new ReservationException("Couldn't confirm quotes!");
        }
        
        //this.quotes.clear();
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }
}