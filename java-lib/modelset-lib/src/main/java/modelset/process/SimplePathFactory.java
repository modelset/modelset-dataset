package modelset.process;

import java.util.List;

import javax.annotation.Nonnull;

import mar.paths.ListofPaths;
import mar.paths.Path;
import mar.paths.PathFactory;
import mar.paths.stemming.IStemmer;
import mar.paths.stemming.IStopWords;
import mar.paths.stemming.ITokenizer;

class SimplePathFactory implements PathFactory {

	@Override
	public IStemmer getStemmer() {
		return IStemmer.IDENTITY;
	}

	@Override
	public IStopWords getStopWords() {
		return new IStopWords() {
			@Override
			public boolean isStopWord(@Nonnull String s) {
				return false;
			}				
		};
	}

	@Override
	public ITokenizer getTokenizer() {
		return ITokenizer.IDENTITY;
	}

	@Override
	public ListofPaths newPathSet(List<? extends Path> arg0) {
		throw new UnsupportedOperationException();
	}
	
}