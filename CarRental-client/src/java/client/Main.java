package client;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.CarType;
import rental.Reservation;
import rental.ReservationConstraints;
import session.CarRentalSessionRemote;
import session.ManagerSessionRemote;

public class Main extends AbstractTestManagement<CarRentalSessionRemote, ManagerSessionRemote> {
    

    public Main(String scriptFile) {
        super(scriptFile);
    }

    public static void main(String[] args) throws Exception {
        // TODO: use updated manager interface to load cars into companies
        Main main = new Main("trips");
        ManagerSessionRemote ms = main.getNewManagerSession("Manager");
        ms.loadCarRentalCompany("hertz.csv");
        ms.loadCarRentalCompany("dockx.csv");
        main.run();
    }

    @Override
    protected Set<String> getBestClients(ManagerSessionRemote ms) throws Exception {
        return ms.getBestClients(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String getCheapestCarType(CarRentalSessionRemote session, Date start, Date end, String region) throws Exception {
        return session.getCheapestCarType(start, end, region); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected CarRentalSessionRemote getNewReservationSession(String name) throws Exception {
        InitialContext context = new InitialContext();
        return (CarRentalSessionRemote) context.lookup(CarRentalSessionRemote.class.getName()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ManagerSessionRemote getNewManagerSession(String name) throws Exception {
        InitialContext context = new InitialContext();
        return (ManagerSessionRemote) context.lookup(ManagerSessionRemote.class.getName()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void getAvailableCarTypes(CarRentalSessionRemote session, Date start, Date end) throws Exception {
        session.getAvailableCarTypes(start, end);
    }

    @Override
    protected void createQuote(CarRentalSessionRemote session, String name, Date start, Date end, String carType, String region) throws Exception {
        ReservationConstraints constraints = new ReservationConstraints(start, end, carType, region);
        session.createQuote(name, constraints);
    }

    @Override
    protected List<Reservation> confirmQuotes(CarRentalSessionRemote session, String name) throws Exception {
        return session.confirmQuotes();
    }

    @Override
    protected int getNumberOfReservationsBy(ManagerSessionRemote ms, String clientName) throws Exception {
        int numberOfReservationsByRenter = 0;
        try {
            numberOfReservationsByRenter = ms.getNumberOfReservationsBy(clientName);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new Exception("Couldn't get the number of reservations by renter.");
        }
        return numberOfReservationsByRenter; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected int getNumberOfReservationsForCarType(ManagerSessionRemote ms, String carRentalName, String carType) throws Exception {
        return ms.getNumberOfReservationsForCarType(carRentalName, carType); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected CarType getMostPopularCarTypeIn(ManagerSessionRemote ms, String carRentalCompanyName, int year) throws Exception {
        return ms.getMostPopularCarTypeIn(carRentalCompanyName, year); //To change body of generated methods, choose Tools | Templates.
    }
}