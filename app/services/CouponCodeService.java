package services;

import models.CouponCode;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Maurice van Veen
 */
@Singleton
public final class CouponCodeService {
    private final play.db.Database database;

    /**
     * Creates a new {@link CouponCodeService}.
     */
    @Inject
    public CouponCodeService(play.db.Database database) {
        this.database = database;
    }

    public List<CouponCode> fetchCouponCodes() {
        return database.withConnection(connection -> {
            List<CouponCode> couponCodes = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM couponcodes");

            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                couponCodes.add(ModelService.createCouponCode(results));
            }

            return couponCodes;
        });
    }

    public Optional<CouponCode> getCouponCode(String code) {
        return database.withConnection(connection -> {
            Optional<CouponCode> couponCode = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM couponcodes WHERE code=?");
            stmt.setString(1, code);

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                couponCode = Optional.of(ModelService.createCouponCode(results));
            }

            return couponCode;
        });
    }
}
