package de.moinapp.moin;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class MoinDaoGenerator {
    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "de.moinapp.moin.db");

        Entity friend = schema.addEntity("Friend");
        friend.addIdProperty();
        friend.addStringProperty("uuid");
        friend.addStringProperty("username");
        friend.addStringProperty("email");

        new DaoGenerator().generateAll(schema, args[0]);
    }
}
