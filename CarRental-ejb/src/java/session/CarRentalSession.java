package session;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.CompanyLoader;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {
    
    @PersistenceContext
    EntityManager em;
    
    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();

    @Override
    public Set<String> getAllRentalCompanies() {
        TypedQuery<String> q = em.createNamedQuery("getAllRentalCompaniesNames", String.class);
        return new HashSet<String>(q.getResultList());
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        List<CarType> availableCarTypes = new LinkedList<CarType>();
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
    public Quote createQuote(String company, ReservationConstraints constraints) throws ReservationException {
        TypedQuery<CarRentalCompany> q = em.createNamedQuery("getCompanyByName", CarRentalCompany.class)
                .setParameter("name", company);
        try {
            CarRentalCompany crc = q.getSingleResult();
            Quote out = crc.createQuote(constraints, renter);
            quotes.add(out);
            return out;
        } catch(Exception e) {
            throw new ReservationException(e);
        }
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();
        TypedQuery<CarRentalCompany> q = em.createNamedQuery("getCompanyByName", CarRentalCompany.class);
        try {
            for (Quote quote : quotes) {
                //set name at the query for each quote
                q.setParameter("name", quote.getRentalCompany());
                CarRentalCompany company = q.getSingleResult();
                done.add(company.confirmQuote(quote));
            }
        } catch (Exception e) {
            for(Reservation r:done) {
                q.setParameter("name", r.getRentalCompany());
                CarRentalCompany company = q.getSingleResult();
                company.cancelReservation(r);
            }
            throw new ReservationException(e);
        }
        
        //save reservations in the db
        for(Reservation res: done) {
            em.persist(res);
        }
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