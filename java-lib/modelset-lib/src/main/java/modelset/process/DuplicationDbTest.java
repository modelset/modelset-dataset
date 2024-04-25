package modelset.process;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import mar.analysis.duplicates.DuplicateFinder.DuplicationGroup;
import mar.analysis.duplicates.EcoreDuplicateFinder;
import mar.modelling.loader.ILoader;
import mar.validation.AnalyserRegistry;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser.Factory;
import modelset.database.PlainFileInfo;

public class DuplicationDbTest {

	public static void main(String[] args) throws IOException, SQLException {
		
	
		String m = "/home/jesus/projects/mde-ml/model-mate/experiments/data/mar/repo-genmymodel-ecore/data/_DmvCgAwjEeWATac42w0kFQ.ecore";

		String m2 = "/home/jesus/projects/mde-ml/model-mate/experiments/data/mar/repo-genmymodel-ecore/data/_Q_PZgLWgEem8I_zdXKdSGw.ecore";
		String m3 = "/home/jesus/projects/mde-ml/model-mate/experiments/data/mar/repo-genmymodel-ecore/data/_4DA1oPokEeSE6sazzutmkA.ecore";

		String m4 = "/home/jesus/projects/mde-ml/model-mate/experiments/data/mar/repo-genmymodel-ecore/data/_u_vNsOQjEeiyGtLb2crGgA.ecore";

		
		EcoreDuplicateFinder<IFileInfo> finder = new EcoreDuplicateFinder<>();
		
		
		Factory factory = AnalyserRegistry.INSTANCE.getFactory("ecore");
		factory.configureEnvironment();
		ILoader loader = factory.newLoader();
		
		
		finder.addResource(toFI("m1"), loader.toEMF(new File(m)));
		finder.addResource(toFI("m2"), loader.toEMF(new File(m2)));
		finder.addResource(toFI("m3"), loader.toEMF(new File(m3)));
		finder.addResource(toFI("m4"), loader.toEMF(new File(m4)));

		
		Collection<DuplicationGroup<IFileInfo>> result = finder.getDuplicates(0.7, 0.8);
		System.out.println("Groups: " + result.size());
		for (DuplicationGroup<IFileInfo> duplicationGroup : result) {
			System.out.println(duplicationGroup);
		}

		DuplicationDatabase db = new DuplicationDatabase(new File("/tmp/dup.db"));
		for (DuplicationGroup<IFileInfo> duplicationGroup : result) {
			db.addGroup("id", duplicationGroup);
		}
		
		db.close();
	}

	private static IFileInfo toFI(String string) {
		return new PlainFileInfo(string, null, null);
	}
}
