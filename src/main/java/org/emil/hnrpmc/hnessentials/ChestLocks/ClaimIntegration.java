package org.emil.hnrpmc.hnessentials.ChestLocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.hnclaim.Claim; // Import deiner Klasse
import org.emil.hnrpmc.hnclaim.HNClaims; // Annahme
import java.util.List;
import java.util.UUID;

public class ClaimIntegration {

    // Prüft, ob ein Spieler an dieser Position sperren darf
    public static boolean canLock(ServerPlayer player, BlockPos pos) {
        try {
            // Zugriff auf deine Claims (Pfad aus deiner Beschreibung)
            List<Claim> claims = HNClaims.getInstance().getClaimManager().getClaims();

            for (Claim claim : claims) {
                if (isPosInClaim(pos, claim)) {
                    // Wenn es im Claim ist, MUSS der Spieler der Owner sein
                    if (!claim.getownerUUID().equals(player.getUUID())) {
                        return false;
                    }
                    return true; // Im eigenen Claim -> Erlaubt
                }
            }
            // Nicht in einem Claim -> Erlaubt (oder false, je nach Server-Regel)
            return true;
        } catch (Exception e) {
            // Falls HNClaims nicht geladen ist, sicherheitshalber erlauben oder loggen
            e.printStackTrace();
            return true;
        }
    }

    private static boolean isPosInClaim(BlockPos pos, Claim claim) {
        try {
            // Parse deine String-Koordinaten (Annahme: "x,y,z")
            int[] c1 = parsePos(claim.getCorner1());
            int[] c2 = parsePos(claim.getCorner2());

            int minX = Math.min(c1[0], c2[0]);
            int maxX = Math.max(c1[0], c2[0]);
            int minZ = Math.min(c1[2], c2[2]);
            int maxZ = Math.max(c1[2], c2[2]);
            // Y-Höhe wird bei Claims oft ignoriert (Bedrock bis Sky),
            // falls nicht, hier Y-Check einfügen.

            return pos.getX() >= minX && pos.getX() <= maxX &&
                    pos.getZ() >= minZ && pos.getZ() <= maxZ;
        } catch (Exception e) {
            return false;
        }
    }

    private static int[] parsePos(String s) {
        String[] parts = s.split(","); // Oder wie auch immer du trennst
        return new int[]{
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim())
        };
    }
}