package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import top.ribs.scguns.network.ClientMessageHandler;

public class S2CMessageUpdateAmmo extends PlayMessage<S2CMessageUpdateAmmo> {
    private int ammoCount;

    public S2CMessageUpdateAmmo() {}

    public S2CMessageUpdateAmmo(int ammoCount) {
        this.ammoCount = ammoCount;
    }

    @Override
    public void encode(S2CMessageUpdateAmmo message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.ammoCount);
    }

    @Override
    public S2CMessageUpdateAmmo decode(FriendlyByteBuf buffer) {
        S2CMessageUpdateAmmo message = new S2CMessageUpdateAmmo();
        message.ammoCount = buffer.readInt();
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateAmmo message, MessageContext context) {
        context.execute(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (ClientMessageHandler.handleUpdateAmmo(message.ammoCount)) {
                    context.setHandled(true);
                }
            });
        });
    }
}